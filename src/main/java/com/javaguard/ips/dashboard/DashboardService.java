package com.javaguard.ips.dashboard;

import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.capture.CapturePipelineService;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.statistics.TrafficStatisticsService;
import com.javaguard.ips.web.dto.DashboardSummaryResponse;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private static final int RECENT_ALERT_LIMIT = 10;


    private final CapturePipelineService capturePipelineService;

    private final TrafficStatisticsService trafficStatisticsService;

    private final AlertService alertService;


    public DashboardService(
            CapturePipelineService capturePipelineService,
            TrafficStatisticsService trafficStatisticsService,
            AlertService alertService
    ) {

        this.capturePipelineService =
                capturePipelineService;

        this.trafficStatisticsService =
                trafficStatisticsService;

        this.alertService =
                alertService;
    }


    public DashboardSummaryResponse getSummary() {

        List<SecurityEvent> recentAlerts =
                alertService
                        .getAlertHistory()
                        .stream()
                        .limit(RECENT_ALERT_LIMIT)
                        .toList();


        return new DashboardSummaryResponse(

                capturePipelineService.getStatus(),

                trafficStatisticsService.snapshot(),

                alertService.getStatistics(),

                recentAlerts
        );
    }
}