package com.javaguard.ips.fiveg.detection.rule;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;
import com.javaguard.ips.fiveg.detection.FiveGDetectionRule;
import com.javaguard.ips.fiveg.gtpu.InnerIpPacketInfo;
import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.policy.SlicePolicyService;
import com.javaguard.ips.fiveg.policy.model.SliceSecurityPolicy;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SlicePolicyViolationRule
        implements FiveGDetectionRule {

    private static final Duration ALERT_COOLDOWN =
            Duration.ofSeconds(30);

    private final SlicePolicyService policyService;

    private final ConcurrentMap<String, PacketRateWindow> rateWindows =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Instant> lastAlerts =
            new ConcurrentHashMap<>();


    public SlicePolicyViolationRule(
            SlicePolicyService policyService
    ) {
        this.policyService = policyService;
    }


    @Override
    public Optional<SecurityEvent> analyze(
            EnrichedNetworkEvent event
    ) {

        if (!event.hasFiveGContext()) {
            return Optional.empty();
        }

        if (!event.hasInnerPacket()) {
            return Optional.empty();
        }

        PduSessionContext session =
                event.pduSession();

        Optional<SliceSecurityPolicy> policyResult =
                policyService.findMatching(session);

        if (policyResult.isEmpty()) {
            return Optional.empty();
        }

        SliceSecurityPolicy policy =
                policyResult.get();

        InnerIpPacketInfo inner =
                event.innerPacket();

        Instant now =
                event.networkEvent()
                        .timestamp();

        String violation = null;
        int evidenceCount = 1;


        if (!policy.allowsProtocol(inner.protocol())) {

            violation =
                    "Protocol "
                            + protocolName(
                            inner.protocol()
                    )
                            + " is not allowed";

        } else if (policy.maxPacketsPerSecond() > 0) {

            String rateKey =
                    session.supi()
                            + "#"
                            + session.pduSessionId()
                            + "#"
                            + policy.id();

            int currentRate =
                    rateWindows
                            .computeIfAbsent(
                                    rateKey,
                                    ignored ->
                                            new PacketRateWindow()
                            )
                            .record(now);

            if (
                    currentRate
                            > policy.maxPacketsPerSecond()
            ) {
                violation =
                        "Packet rate "
                                + currentRate
                                + " pps exceeds limit "
                                + policy.maxPacketsPerSecond()
                                + " pps";

                evidenceCount =
                        currentRate;
            }
        }


        if (violation == null) {
            return Optional.empty();
        }


        String alertKey =
                session.supi()
                        + "#"
                        + session.pduSessionId()
                        + "#"
                        + policy.id()
                        + "#"
                        + violationType(
                        violation
                );

        Instant previous =
                lastAlerts.get(alertKey);

        if (
                previous != null
                        && now.isBefore(
                        previous.plus(
                                ALERT_COOLDOWN
                        )
                )
        ) {
            return Optional.empty();
        }

        lastAlerts.put(
                alertKey,
                now
        );


        String description =
                "5G slice policy violation: "
                        + violation
                        + "; policy="
                        + policy.name()
                        + "; SUPI="
                        + session.supi()
                        + "; PDU session="
                        + session.pduSessionId()
                        + "; DNN="
                        + session.dnn()
                        + "; slice="
                        + session.slice().displayName();


        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        now,
                        ThreatType.SLICE_POLICY_VIOLATION,
                        policy.severity(),
                        inner.sourceIp(),
                        inner.destinationIp(),
                        description,
                        evidenceCount
                )
        );
    }


    private String protocolName(
            int protocol
    ) {

        return switch (protocol) {
            case 6 -> "TCP";
            case 17 -> "UDP";
            case 1 -> "ICMP";
            case 58 -> "ICMPv6";
            default -> "IP protocol " + protocol;
        };
    }


    private String violationType(
            String violation
    ) {

        if (
                violation.startsWith(
                        "Packet rate"
                )
        ) {
            return "RATE";
        }

        return "PROTOCOL";
    }


    private static class PacketRateWindow {

        private long epochSecond = -1;

        private int packets;


        public synchronized int record(
                Instant timestamp
        ) {

            long currentSecond =
                    timestamp.getEpochSecond();

            if (
                    currentSecond != epochSecond
            ) {
                epochSecond = currentSecond;

                packets = 0;
            }

            packets++;

            return packets;
        }
    }
}