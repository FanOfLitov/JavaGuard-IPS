package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.config.TcpDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.support.CooldownTracker;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;
import com.javaguard.ips.flow.tcp.TcpFlowTracker;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class HalfOpenConnectionRule
        implements DetectionRule {

    private final TcpFlowTracker flowTracker;

    private final TcpDetectionProperties properties;

    private final CooldownTracker cooldown =
            new CooldownTracker();


    public HalfOpenConnectionRule(
            TcpFlowTracker flowTracker,
            TcpDetectionProperties properties
    ) {
        this.flowTracker = flowTracker;
        this.properties = properties;
    }


    @Override
    public Optional<SecurityEvent> analyze(
            NetworkEvent event
    ) {

        if (event.protocol() != NetworkProtocol.TCP) {
            return Optional.empty();
        }

        /*
         * Проверяем при появлении нового SYN.
         */
        if (!event.tcpSyn() || event.tcpAck()) {
            return Optional.empty();
        }

        TcpDetectionProperties.HalfOpen config =
                properties.getHalfOpen();

        int halfOpen =
                flowTracker
                        .countHalfOpenByClient(
                                event.sourceIp(),
                                event.timestamp(),
                                config.getWindow()
                        );

        if (halfOpen < config.getThreshold()) {
            return Optional.empty();
        }

        if (!cooldown.tryAcquire(
                event.sourceIp(),
                event.timestamp(),
                config.getCooldown()
        )) {
            return Optional.empty();
        }

        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.HALF_OPEN_CONNECTION_FLOOD,
                        Severity.CRITICAL,
                        event.sourceIp(),
                        event.destinationIp(),
                        "TCP half-open connection flood detected: "
                                + halfOpen
                                + " incomplete connections within "
                                + config.getWindow().toSeconds()
                                + " seconds",
                        halfOpen
                )
        );
    }
}