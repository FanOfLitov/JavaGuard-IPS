package com.javaguard.ips.alert;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.persistence.SecurityAlertStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AlertServiceTest {

    @Test
    void shouldStoreAlertInMemoryAndPersistence() {

        SecurityAlertStore alertStore =
                mock(SecurityAlertStore.class);

        AlertService alertService =
                new AlertService(alertStore);

        SecurityEvent event =
                new SecurityEvent(
                        UUID.randomUUID(),
                        Instant.parse(
                                "2026-09-30T12:00:00Z"
                        ),
                        ThreatType.PORT_SCAN,
                        Severity.HIGH,
                        "10.0.0.10",
                        "10.0.0.20",
                        "Test port scan",
                        15
                );

        alertService.publish(event);

        assertEquals(
                1,
                alertService
                        .getRecentAlerts()
                        .size()
        );

        assertEquals(
                event,
                alertService
                        .getRecentAlerts()
                        .getFirst()
        );

        verify(alertStore)
                .save(event);
    }
}