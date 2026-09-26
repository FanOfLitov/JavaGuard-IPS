package com.javaguard.ips.detection.rule;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PortScanRuleTest {

    @Test
    void shouldDetectPortScanAfterFifteenDistinctPorts() {

        PortScanRule rule =
                new PortScanRule();

        Instant startTime =
                Instant.parse("2026-09-25T10:00:00Z");

        Optional<SecurityEvent> result =
                Optional.empty();

        for (int i = 0; i < 15; i++) {

            NetworkEvent event =
                    new NetworkEvent(
                            startTime.plusMillis(i * 100L),
                            "10.0.0.10",
                            "10.0.0.20",
                            40_000 + i,
                            20 + i,
                            NetworkProtocol.TCP,
                            60,
                            true,
                            false,
                            false,
                            false
                    );

            result =
                    rule.analyze(event);

            if (i < 14) {
                assertTrue(result.isEmpty());
            }
        }

        assertTrue(result.isPresent());

        SecurityEvent alert =
                result.orElseThrow();

        assertEquals(
                ThreatType.PORT_SCAN,
                alert.type()
        );

        assertEquals(
                Severity.HIGH,
                alert.severity()
        );

        assertEquals(
                "10.0.0.10",
                alert.sourceIp()
        );

        assertEquals(
                "10.0.0.20",
                alert.destinationIp()
        );

        assertEquals(
                15,
                alert.evidenceCount()
        );
    }

    @Test
    void shouldNotDetectRepeatedConnectionsToSamePort() {

        PortScanRule rule =
                new PortScanRule();

        Instant startTime =
                Instant.parse("2026-09-25T10:00:00Z");

        for (int i = 0; i < 30; i++) {

            NetworkEvent event =
                    new NetworkEvent(
                            startTime.plusMillis(i * 100L),
                            "10.0.0.10",
                            "10.0.0.20",
                            40_000 + i,
                            443,
                            NetworkProtocol.TCP,
                            60,
                            true,
                            false,
                            false,
                            false
                    );

            Optional<SecurityEvent> result =
                    rule.analyze(event);

            assertTrue(result.isEmpty());
        }
    }
}