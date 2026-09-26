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
public class SynFloodRuleTest {

    @Test
    void shouldDetectSynFloodAfterFiftySynPackets() {
        SynFloodRule rule = new SynFloodRule();

        Instant startTime = Instant.parse("2026-09-26T12:00:00Z");

        Optional<SecurityEvent> result = Optional.empty();

        for (int i = 0; i < 50; i++) {
            NetworkEvent event = createSynEvent(
                    startTime.plusMillis(i * 50L)
            );
            result = rule.analyze(event);

            if (i < 49) {
                assertTrue(
                        result.isEmpty()
                );

            }
        }


        assertTrue(
                result.isPresent()
        );

        SecurityEvent alert = result.orElseThrow();

        assertEquals(
                ThreatType.SYN_FLOOD,
                alert.type()
        );

        assertEquals(
                Severity.HIGH,
                alert.severity()
        );

        assertEquals(
                50, alert.evidenceCount()
        );

    }

    @Test
    void shouldIgnoreSynAckPackets(){
        SynFloodRule rule = new SynFloodRule();

        Instant startTime = Instant.parse("2026-09-26T12:00:00Z");

        for(int i =0;i<100;i++){
            NetworkEvent event = createSynAckEvent(
                    startTime.plusMillis(i*20L)
            );

            Optional<SecurityEvent> result = rule.analyze(event);

            assertTrue(
                    result.isEmpty()
            );
        }
    }

    private NetworkEvent createSynEvent(
            Instant timestamp
    ){
        return new NetworkEvent(
                timestamp,
                "10.0.0.10",
                "10.0.0.20",
                40_000,
                443,
                NetworkProtocol.TCP,
                60,
                true,
                false,
                false,
                false

        );
    }

    private NetworkEvent createSynAckEvent(
            Instant timestamp
    ){
        return new NetworkEvent(
                timestamp,
                "10.0.0.20",
                "10.0.0.10",
                443,
                40_000,
                NetworkProtocol.TCP,
                60,
                true,
                true,
                false,
                false
        );
    }
}
