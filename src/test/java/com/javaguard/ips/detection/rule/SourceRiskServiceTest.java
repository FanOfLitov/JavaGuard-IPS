package com.javaguard.ips.detection.risk;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceRiskServiceTest {

    @Test
    void shouldIncreaseRiskForDifferentThreatTypes() {

        RiskProperties properties =
                new RiskProperties();

        SourceRiskService service =
                new SourceRiskService(
                        properties
                );

        String attacker =
                "198.51.100.50";

        Instant now =
                Instant.now();


        service.record(
                new SecurityEvent(
                        UUID.randomUUID(),
                        now,
                        ThreatType.PORT_SCAN,
                        Severity.HIGH,
                        attacker,
                        "10.0.0.1",
                        "test",
                        15
                )
        );


        SourceRiskSnapshot result =
                service.record(
                        new SecurityEvent(
                                UUID.randomUUID(),
                                now.plusSeconds(1),
                                ThreatType.UDP_FLOOD,
                                Severity.HIGH,
                                attacker,
                                "10.0.0.1",
                                "test",
                                500
                        )
                );


        assertEquals(
                RiskLevel.CRITICAL,
                result.level()
        );

        assertTrue(
                result.score()
                        >= properties.getBlockThreshold()
        );
    }
}