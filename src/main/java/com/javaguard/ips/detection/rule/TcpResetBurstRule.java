package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.config.TcpDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.support.CooldownTracker;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;
import com.javaguard.ips.flow.tcp.TcpFlowSnapshot;
import com.javaguard.ips.flow.tcp.TcpFlowTracker;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TcpResetBurstRule
        implements DetectionRule {

    private final TcpFlowTracker flowTracker;

    private final TcpDetectionProperties properties;

    private final CooldownTracker cooldown =
            new CooldownTracker();


    public TcpResetBurstRule(
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

        if (event.protocol() != NetworkProtocol.TCP
                || !event.tcpRst()) {

            return Optional.empty();
        }

        Optional<TcpFlowSnapshot> flowResult =
                flowTracker.find(event);

        if (flowResult.isEmpty()) {
            return Optional.empty();
        }

        TcpFlowSnapshot flow =
                flowResult.get();

        String clientIp =
                flow.key().clientIp();

        TcpDetectionProperties.ResetBurst config =
                properties.getResetBurst();

        int resetFlows =
                flowTracker
                        .countRecentResetsByClient(
                                clientIp,
                                event.timestamp(),
                                config.getWindow()
                        );

        if (resetFlows < config.getThreshold()) {
            return Optional.empty();
        }

        if (!cooldown.tryAcquire(
                clientIp,
                event.timestamp(),
                config.getCooldown()
        )) {
            return Optional.empty();
        }

        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.TCP_RESET_BURST,
                        Severity.HIGH,
                        clientIp,
                        flow.key().serverIp(),
                        "TCP reset burst detected: "
                                + resetFlows
                                + " reset flows within "
                                + config.getWindow().toSeconds()
                                + " seconds",
                        resetFlows
                )
        );
    }
}