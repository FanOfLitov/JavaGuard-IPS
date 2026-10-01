package com.javaguard.ips.web;

import com.javaguard.ips.dashboard.DashboardService;
import com.javaguard.ips.web.dto.DashboardSummaryResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;


    public DashboardController(
            DashboardService dashboardService
    ) {

        this.dashboardService =
                dashboardService;
    }


    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {

        return dashboardService.getSummary();
    }
}