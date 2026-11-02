package com.javaguard.ips.fiveg.detection.rule;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;
import com.javaguard.ips.fiveg.correlation.FiveGDirection;

import com.javaguard.ips.fiveg.detection.FiveGDetectionRule;

import com.javaguard.ips.fiveg.gtpu.InnerIpPacketInfo;

import com.javaguard.ips.fiveg.model.PduSessionContext;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UeIpMismatchRule
        implements FiveGDetectionRule {

    private static final Duration COOLDOWN =
            Duration.ofSeconds(30);

    private final ConcurrentMap<String, Instant> lastAlerts =
            new ConcurrentHashMap<>();

    @Override
    public Optional<SecurityEvent> analyze(
            EnrichedNetworkEvent event
    ) {

        if (!event.isGtpU()) {
            return Optional.empty();
        }

        if (!event.hasFiveGContext()) {
            return Optional.empty();
        }

        if (!event.hasInnerPacket()) {
            return Optional.empty();
        }

        PduSessionContext context =
                event.pduSession();

        InnerIpPacketInfo inner =
                event.innerPacket();

        String expectedUeIp =
                context.ueIp();

        String observedUeIp;

        if (event.direction() == FiveGDirection.UPLINK) {
            observedUeIp = inner.sourceIp();

        } else if (event.direction() == FiveGDirection.DOWNLINK) {
            observedUeIp = inner.destinationIp();

        } else {
            return Optional.empty();
        }

        if (expectedUeIp.equals(observedUeIp)) {
            return Optional.empty();
        }

        String key =
                context.supi()
                        + "#"
                        + context.pduSessionId()
                        + "#"
                        + event.direction();

        Instant now =
                event.networkEvent()
                        .timestamp();

        Instant previous =
                lastAlerts.get(key);

        if (previous != null
                && now.isBefore(
                previous.plus(COOLDOWN)
        )) {
            return Optional.empty();
        }

        lastAlerts.put(key, now);

        String description = "UE IP mismatch for SUPI "
                        + context.supi()
                        + ": expected "
                        + expectedUeIp
                        + ", observed "
                        + observedUeIp
                        + ", TEID "
                        + Long.toUnsignedString(
                        event.gtpUHeader().teid()
                )
                        + ", direction "
                        + event.direction();

        return Optional.of(new SecurityEvent(
                        UUID.randomUUID(),
                        now,
                        ThreatType.UE_IP_MISMATCH,
                        Severity.HIGH,
                        event.networkEvent().sourceIp(),
                        event.networkEvent().destinationIp(),
                        description,
                        1
                )
        );
    }
}