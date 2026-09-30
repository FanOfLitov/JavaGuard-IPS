package com.javaguard.ips.web;

import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.detection.model.SecurityEvent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}