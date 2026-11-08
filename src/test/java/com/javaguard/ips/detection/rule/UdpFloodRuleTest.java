package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.config.BehavioralDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UdpFloodRuleTest {

    @Test
    void shouldDetectUdpFlood() {

        BehavioralDetectionProperties properties =
                new BehavioralDetectionProperties();

        properties.getUdpFlood()
                .setThreshold(5);

        properties.getUdpFlood()
                .setWindow(
                        Duration.ofSeconds(3)
                );

        UdpFloodRule rule =
                new UdpFloodRule(
                        properties
                );

        Instant start =
                Instant.now();

        Optional<SecurityEvent> detected =
                Optional.empty();

        for (int i = 0; i < 5; i++) {

            NetworkEvent event =
                    new NetworkEvent(
                            start.plusMillis(i * 100L),
                            "198.51.100.10",
                            "10.0.0.1",
                            40000,
                            9999,
                            NetworkProtocol.UDP,
                            512,
                            false,
                            false,
                            false,
                            false
                    );

            detected =
                    rule.analyze(event);
        }

        assertTrue(
                detected.isPresent()
        );
    }
}