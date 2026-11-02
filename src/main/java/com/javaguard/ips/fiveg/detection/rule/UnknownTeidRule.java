package com.javaguard.ips.fiveg.detection.rule;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;
import com.javaguard.ips.fiveg.detection.FiveGDetectionRule;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
public class UnknownTeidRule implements FiveGDetectionRule {

    private static final Duration COOLDOWN = Duration.ofSeconds(30);


    private final ConcurrentMap<String, Instant> lastAlerts = new ConcurrentHashMap<>();


    @Override
    public Optional<SecurityEvent> analyze(
            EnrichedNetworkEvent event
    ) {

        if (!event.isGtpU()) {

            return Optional.empty();
        }


        if (
                !event.gtpUHeader()
                        .isTPdu()
        ) {

            return Optional.empty();
        }


        if (
                !event.gtpUHeader()
                        .hasTeid()
        ) {

            return Optional.empty();
        }


        if (
                event.hasFiveGContext()
        ) {

            return Optional.empty();
        }


        long teid =
                event.gtpUHeader()
                        .teid();


        String key =
                event.networkEvent()
                        .sourceIp()
                        + "#"
                        + Long.toUnsignedString(
                        teid
                );


        Instant now = event.networkEvent().timestamp();


        Instant previous = lastAlerts.get(key);


        if (previous != null
                        &&
                        now.isBefore(
                                previous.plus(
                                        COOLDOWN
                                )
                        )
        ) {

            return Optional.empty();
        }


        lastAlerts.put(key, now);


        SecurityEvent securityEvent =
                new SecurityEvent(

                        UUID.randomUUID(),

                        now,

                        ThreatType.UNKNOWN_GTP_TEID,

                        Severity.MEDIUM,

                        event.networkEvent()
                                .sourceIp(),

                        event.networkEvent()
                                .destinationIp(),

                        "GTP-U T-PDU observed with unknown TEID "
                                + Long.toUnsignedString(
                                teid
                        ),

                        1
                );


        return Optional.of(
                securityEvent
        );
    }
}