package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.config.BehavioralDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.support.CooldownTracker;
import com.javaguard.ips.detection.support.DistinctSlidingWindow;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

@Component
public class HorizontalScanRule implements DetectionRule {

    private final BehavioralDetectionProperties properties;

    private final DistinctSlidingWindow destinations =
            new DistinctSlidingWindow();

    private final CooldownTracker cooldown =
            new CooldownTracker();


    public HorizontalScanRule(
            BehavioralDetectionProperties properties
    ) {
        this.properties = properties;
    }


    @Override
    public Optional<SecurityEvent> analyze(
            NetworkEvent event
    ) {

        if (event.protocol() != NetworkProtocol.TCP) {
            return Optional.empty();
        }

        if (!event.tcpSyn() || event.tcpAck()) {
            return Optional.empty();
        }

        if (event.destinationPort() == null) {
            return Optional.empty();
        }

        BehavioralDetectionProperties.HorizontalScan config =
                properties.getHorizontalScan();

        String key =
                event.sourceIp()
                        + "#"
                        + event.destinationPort();

        int distinctHosts =
                destinations.record(
                        key,
                        event.destinationIp(),
                        event.timestamp(),
                        config.getWindow()
                );

        if (distinctHosts < config.getDistinctDestinations()) {
            return Optional.empty();
        }

        if (!cooldown.tryAcquire(
                key,
                event.timestamp(),
                config.getCooldown()
        )) {
            return Optional.empty();
        }

        String description =
                "Horizontal scan detected: source contacted "
                        + distinctHosts
                        + " destination hosts on TCP port "
                        + event.destinationPort()
                        + " within "
                        + config.getWindow().toSeconds()
                        + " seconds";

        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.HORIZONTAL_SCAN,
                        Severity.HIGH,
                        event.sourceIp(),
                        event.destinationIp(),
                        description,
                        distinctHosts
                )
        );
    }


    @Scheduled(fixedDelay = 60_000)
    public void cleanup() {

        Instant now = Instant.now();

        destinations.cleanup(
                now,
                Duration.ofMinutes(5)
        );

        cooldown.cleanup(
                now,
                Duration.ofMinutes(5)
        );
    }
}