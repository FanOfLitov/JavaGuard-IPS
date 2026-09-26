package com.javaguard.ips.detection;

import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.detection.rule.PortScanRule;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectionEngineTest {

    @Test
    void shouldPublishAlertWhenPortScanIsDetected(){
        AlertService alertService = new AlertService();
        PortScanRule portScanRule = new PortScanRule();

        DetectionEngine detectionEngine = new DetectionEngine(
                List.of(portScanRule),
                alertService);
        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");

        for(int i=0;i<15;i++){
            NetworkEvent event = createSynEvent(
                    startTime.plusMillis(i*100L),
                    20+i
            );

            detectionEngine.analyze(event);
        }
        List<SecurityEvent> alerts= alertService.getRecentAlerts();

        assertEquals(
                1,
                alerts.size()
        );

        SecurityEvent alert = alerts.getFirst();

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
    void shouldNotPublishAlertForNornalTraffic(){
        AlertService alertService = new AlertService();
        PortScanRule portScanRule = new PortScanRule();
        DetectionEngine detectionEngine = new DetectionEngine(List.of(portScanRule),
                alertService
        );

        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");

        for(int i=0;i<30;i++){
            NetworkEvent event = createSynEvent(
                    startTime.plusMillis(i * 100L),
                    443
            );

            detectionEngine.analyze(event);
        }

        List<SecurityEvent> alerts = alertService.getRecentAlerts();

        assertTrue(alerts.isEmpty());

    }

    private NetworkEvent createSynEvent(Instant timestamp, int destinationPort){
        return new NetworkEvent(
                timestamp,
                "10.0.0.10",
                "10.0.0.20",
                40_000,
                destinationPort,
                NetworkProtocol.TCP,
                60,
                true,
                false,
                false,
                false
        );
    }

}