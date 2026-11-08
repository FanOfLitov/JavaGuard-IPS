package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.config.BehavioralDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.support.CooldownTracker;
import com.javaguard.ips.detection.support.SlidingWindowCounter;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

@Component
public class UdpFloodRule implements DetectionRule {

    private final BehavioralDetectionProperties properties;

    private final SlidingWindowCounter counter =
            new SlidingWindowCounter();

    private final CooldownTracker cooldown =
            new CooldownTracker();


    public UdpFloodRule(
            BehavioralDetectionProperties properties
    ) {
        this.properties = properties;
    }


    @Override
    public Optional<SecurityEvent> analyze(
            NetworkEvent event
    ) {

        if (event.protocol() != NetworkProtocol.UDP) {
            return Optional.empty();
        }

        BehavioralDetectionProperties.FloodRule config =
                properties.getUdpFlood();

        String key =
                event.sourceIp()
                        + "->"
                        + event.destinationIp();

        int count =
                counter.record(
                        key,
                        event.timestamp(),
                        config.getWindow()
                );

        if (count < config.getThreshold()) {
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
                "UDP flood detected: "
                        + count
                        + " packets within "
                        + config.getWindow().toSeconds()
                        + " seconds";

        return Optional.of(
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.UDP_FLOOD,
                        Severity.HIGH,
                        event.sourceIp(),
                        event.destinationIp(),
                        description,
                        count
                )
        );
    }


    @Scheduled(fixedDelay = 60_000)
    public void cleanup() {

        Instant now = Instant.now();

        counter.cleanup(
                now,
                Duration.ofMinutes(5)
        );

        cooldown.cleanup(
                now,
                Duration.ofMinutes(5)
        );
    }
}