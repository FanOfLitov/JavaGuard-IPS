package com.javaguard.ips.web;

import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.statistics.SecurityStatisticsSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.web.dto.AlertPageResponse;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;


    public AlertController(
            AlertService alertService
    ) {

        this.alertService = alertService;
    }


    @GetMapping
    public List<SecurityEvent> alerts() {

        return alertService.getRecentAlerts();
    }


    @GetMapping("/history")
    public List<SecurityEvent> history() {

        return alertService.getAlertHistory();
    }


    @GetMapping("/history/count")
    public Map<String, Long> count() {

        return Map.of(
                "count",
                alertService.getStoredAlertCount()
        );
    }

    @GetMapping("/search")
    public AlertPageResponse search(
            @RequestParam(required = false)
            ThreatType type,

            @RequestParam(required = false)
            Severity severity,

            @RequestParam(required = false)
            String sourceIp,

            @RequestParam(required = false)
            String destinationIp,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        int safePage =
                Math.max(
                        page,
                        0
                );

        int safeSize =
                Math.min(
                        Math.max(
                                size,
                                1
                        ),
                        100
                );

        Page<SecurityEvent> result =
                alertService.searchAlerts(
                        type,
                        severity,
                        sourceIp,
                        destinationIp,
                        safePage,
                        safeSize
                );

        return new AlertPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
    @GetMapping("/statistics")
    public SecurityStatisticsSnapshot statistics() {

        return alertService.getStatistics();
    }
}