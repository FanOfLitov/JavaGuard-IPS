package com.javaguard.ips.web.dto;
import com.javaguard.ips.capture.CapturePipelineStatus;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.statistics.SecurityStatisticsSnapshot;
import com.javaguard.ips.statistics.TrafficStatisticsSnapshot;

import java.util.List;
public record DashboardSummaryResponse(

        CapturePipelineStatus capture,

        TrafficStatisticsSnapshot traffic,

        SecurityStatisticsSnapshot security,

        List<SecurityEvent> recentAlerts

) {
}
