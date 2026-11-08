package com.javaguard.ips.detection.risk;

import com.javaguard.ips.detection.model.ThreatType;

import java.time.Instant;
import java.util.Set;

public record SourceRiskSnapshot(
        String sourceIp,
        int score,
        RiskLevel level,
        long totalEvents,
        Set<ThreatType> threatTypes,
        Instant lastEventAt
) {
}