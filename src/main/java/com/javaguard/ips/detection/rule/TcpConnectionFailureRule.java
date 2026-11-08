package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.config.TcpDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.support.CooldownTracker;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;
import com.javaguard.ips.flow.tcp.TcpClientFlowStats;
import com.javaguard.ips.flow.tcp.TcpFlowTracker;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TcpConnectionFailureRule
        implements DetectionRule {

    private final TcpFlowTracker flowTracker;

    private final TcpDetectionProperties properties;

    private final CooldownTracker cooldown =
            new CooldownTracker();


    public TcpConnectionFailureRule(
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

        if (!event.tcpSyn() && !event.tcpRst()) {
            return Optional.empty();
        }


        String clientIp =
                resolveClientIp(event);

        if (clientIp == null) {
            return Optional.empty();
        }


        TcpDetectionProperties.FailureRatio config =
                properties.getFailureRatio();


        TcpClientFlowStats stats =
                flowTracker.getClientStats(
                        clientIp,
                        event.timestamp(),
                        config.getWindow()
                );


        if (stats.totalFlows()
                < config.getMinimumFlows()) {

            return Optional.empty();
        }


        if (stats.failureRatio()
                < config.getThreshold()) {

            return Optional.empty();
        }


        if (!cooldown.tryAcquire(
                clientIp,
                event.timestamp(),
                config.getCooldown()
        )) {
            return Optional.empty();
        }


        String percentage =
                String.format(
                        "%.1f",
                        stats.failureRatio() * 100.0
                );


        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.TCP_CONNECTION_FAILURE_ANOMALY,
                        Severity.MEDIUM,
                        clientIp,
                        event.destinationIp(),
                        "Abnormally high TCP connection failure ratio: "
                                + percentage
                                + "%; flows="
                                + stats.totalFlows()
                                + ", halfOpen="
                                + stats.halfOpenFlows()
                                + ", resets="
                                + stats.resetFlows(),
                        stats.halfOpenFlows()
                                + stats.resetFlows()
                )
        );
    }


    private String resolveClientIp(
            NetworkEvent event
    ) {

        if (event.tcpSyn() && !event.tcpAck()) {
            return event.sourceIp();
        }

        return flowTracker
                .find(event)
                .map(
                        flow ->
                                flow.key()
                                        .clientIp()
                )
                .orElse(null);
    }
}