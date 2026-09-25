package com.javaguard.ips.web;

import com.javaguard.ips.statistics.TrafficStatisticsService;
import com.javaguard.ips.statistics.TrafficStatisticsSnapshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
public class TrafficStatisticsController {
    private final TrafficStatisticsService trafficStatisticsService;

    public TrafficStatisticsController(
            TrafficStatisticsService trafficStatisticsService
    ){
        this.trafficStatisticsService = trafficStatisticsService;
    }

    @GetMapping
    public TrafficStatisticsSnapshot statistics(){
        return trafficStatisticsService.snapshot();
    }
}


