package com.javaguard.ips.detection.risk;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SourceRiskService {

    private final RiskProperties properties;

    private final ConcurrentMap<String, RiskState> states =
            new ConcurrentHashMap<>();


    public SourceRiskService(
            RiskProperties properties
    ) {
        this.properties = properties;
    }


    public SourceRiskSnapshot record(
            SecurityEvent event
    ) {

        if (event == null
                || event.sourceIp() == null
                || event.sourceIp().isBlank()) {

            throw new IllegalArgumentException(
                    "Security event must contain source IP"
            );
        }

        RiskState state =
                states.computeIfAbsent(
                        event.sourceIp(),
                        RiskState::new
                );

        return state.record(
                event,
                properties
        );
    }


    public SourceRiskSnapshot get(
            String sourceIp
    ) {

        RiskState state =
                states.get(sourceIp);

        if (state == null) {
            return new SourceRiskSnapshot(
                    sourceIp,
                    0,
                    RiskLevel.LOW,
                    0,
                    Set.of(),
                    null
            );
        }

        return state.snapshot(
                Instant.now(),
                properties
        );
    }


    public List<SourceRiskSnapshot> findAll() {

        Instant now = Instant.now();

        return states
                .values()
                .stream()
                .map(
                        state ->
                                state.snapshot(
                                        now,
                                        properties
                                )
                )
                .filter(
                        snapshot ->
                                snapshot.score() > 0
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        SourceRiskSnapshot::score
                                )
                                .reversed()
                )
                .toList();
    }


    @Scheduled(fixedDelay = 60_000)
    public void cleanup() {

        Instant now = Instant.now();

        states.entrySet().removeIf(
                entry ->
                        entry
                                .getValue()
                                .canExpire(
                                        now,
                                        properties
                                )
        );
    }


    private static class RiskState {

        private final String sourceIp;

        private int score;

        private long totalEvents;

        private final Set<ThreatType> threatTypes =
                EnumSet.noneOf(
                        ThreatType.class
                );

        private Instant lastDecayAt;

        private Instant lastEventAt;


        private RiskState(
                String sourceIp
        ) {
            this.sourceIp = sourceIp;
        }


        private synchronized SourceRiskSnapshot record(
                SecurityEvent event,
                RiskProperties properties
        ) {

            Instant now =
                    event.timestamp() == null
                            ? Instant.now()
                            : event.timestamp();

            applyDecay(
                    now,
                    properties
            );

            boolean newThreatType =
                    !threatTypes.contains(
                            event.type()
                    );

            int points =
                    severityPoints(
                            event.severity()
                    );

            if (newThreatType
                    && !threatTypes.isEmpty()) {

                points +=
                        properties.getDiversityBonus();
            }

            score =
                    Math.min(
                            properties.getMaxScore(),
                            score + points
                    );

            threatTypes.add(
                    event.type()
            );

            totalEvents++;

            lastEventAt = now;

            if (lastDecayAt == null) {
                lastDecayAt = now;
            }

            return snapshotInternal();
        }


        private synchronized SourceRiskSnapshot snapshot(
                Instant now,
                RiskProperties properties
        ) {

            applyDecay(
                    now,
                    properties
            );

            return snapshotInternal();
        }


        private synchronized boolean canExpire(
                Instant now,
                RiskProperties properties
        ) {

            applyDecay(
                    now,
                    properties
            );

            if (lastEventAt == null) {
                return true;
            }

            return score == 0
                    && lastEventAt
                    .plus(
                            properties.getStateTtl()
                    )
                    .isBefore(now);
        }


        private void applyDecay(
                Instant now,
                RiskProperties properties
        ) {

            if (lastDecayAt == null) {
                lastDecayAt = now;
                return;
            }

            long minutes =
                    Duration
                            .between(
                                    lastDecayAt,
                                    now
                            )
                            .toMinutes();

            if (minutes <= 0) {
                return;
            }

            long decay =
                    minutes
                            * properties
                            .getDecayPointsPerMinute();

            score =
                    (int) Math.max(
                            0,
                            score - decay
                    );

            lastDecayAt =
                    lastDecayAt.plus(
                            Duration.ofMinutes(
                                    minutes
                            )
                    );
        }


        private int severityPoints(
                Severity severity
        ) {

            return switch (severity) {
                case LOW -> 10;
                case MEDIUM -> 20;
                case HIGH -> 35;
                case CRITICAL -> 60;
            };
        }


        private SourceRiskSnapshot snapshotInternal() {

            return new SourceRiskSnapshot(
                    sourceIp,
                    score,
                    riskLevel(score),
                    totalEvents,
                    Set.copyOf(
                            threatTypes
                    ),
                    lastEventAt
            );
        }


        private RiskLevel riskLevel(
                int score
        ) {

            if (score >= 80) {
                return RiskLevel.CRITICAL;
            }

            if (score >= 60) {
                return RiskLevel.HIGH;
            }

            if (score >= 30) {
                return RiskLevel.ELEVATED;
            }

            return RiskLevel.LOW;
        }
    }
}