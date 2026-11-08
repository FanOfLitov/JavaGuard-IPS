package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.config.BehavioralDetectionProperties;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HorizontalScanRuleTest {

    @Test
    void shouldDetectHorizontalScan() {

        BehavioralDetectionProperties properties =
                new BehavioralDetectionProperties();

        properties
                .getHorizontalScan()
                .setDistinctDestinations(5);

        HorizontalScanRule rule =
                new HorizontalScanRule(
                        properties
                );

        Instant start =
                Instant.now();

        Optional<SecurityEvent> result =
                Optional.empty();

        for (int i = 1; i <= 5; i++) {

            NetworkEvent event =
                    new NetworkEvent(
                            start.plusMillis(i * 100L),
                            "198.51.100.20",
                            "10.0.0." + i,
                            50000,
                            22,
                            NetworkProtocol.TCP,
                            60,
                            true,
                            false,
                            false,
                            false
                    );

            result =
                    rule.analyze(event);
        }

        assertTrue(
                result.isPresent()
        );
    }
}