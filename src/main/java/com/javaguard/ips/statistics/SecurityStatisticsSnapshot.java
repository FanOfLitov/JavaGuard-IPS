package com.javaguard.ips.statistics;

import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import java.util.List;
import java.util.Map;

public record SecurityStatisticsSnapshot(

        long totalAlerts,

        long alertsLast24Hours,

        Map<ThreatType, Long> byThreatType,

        Map<Severity, Long> bySeverity,

        List<SourceIpCount> topSourceIps

) {

    public record SourceIpCount(
            String sourceIp,
            long count
    ) {
    }
}