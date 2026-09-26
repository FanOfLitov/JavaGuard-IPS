package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SynFloodRule implements DetectionRule {

    private static final int SYN_THRESHOLD = 50;

    private static final Duration WINDOW =
            Duration.ofSeconds(5);

    private static final Duration ALERT_COOLDOWN =
            Duration.ofSeconds(30);

    private final Map<FlowKey, SynFloodWindow> windows =
            new ConcurrentHashMap<>();

    private final Map<FlowKey, Instant> lastAlerts =
            new ConcurrentHashMap<>();

    @Override
    public Optional<SecurityEvent> analyze(
            NetworkEvent event
    ) {

        if (event.protocol() != NetworkProtocol.TCP) {
            return Optional.empty();
        }

        if (!event.tcpSyn()) {
            return Optional.empty();
        }

        if (event.tcpAck()) {
            return Optional.empty();
        }

        FlowKey key =
                new FlowKey(
                        event.sourceIp(),
                        event.destinationIp()
                );

        SynFloodWindow window =
                windows.computeIfAbsent(
                        key,
                        ignored -> new SynFloodWindow()
                );

        int synCount =
                window.record(
                        event.timestamp(),
                        WINDOW
                );

        if (synCount < SYN_THRESHOLD) {
            return Optional.empty();
        }

        Instant lastAlert =
                lastAlerts.get(key);

        if (
                lastAlert != null
                        &&
                        Duration
                                .between(
                                        lastAlert,
                                        event.timestamp()
                                )
                                .compareTo(ALERT_COOLDOWN) < 0
        ) {

            return Optional.empty();
        }

        lastAlerts.put(
                key,
                event.timestamp()
        );

        window.clear();

        SecurityEvent securityEvent =
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.SYN_FLOOD,
                        Severity.HIGH,
                        event.sourceIp(),
                        event.destinationIp(),
                        "TCP SYN flood detected: "
                                + synCount
                                + " SYN packets within "
                                + WINDOW.toSeconds()
                                + " seconds",
                        synCount
                );

        return Optional.of(
                securityEvent
        );
    }

    private record FlowKey(
            String sourceIp,
            String destinationIp
    ) {
    }
}