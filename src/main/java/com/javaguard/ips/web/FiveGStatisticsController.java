package com.javaguard.ips.web;

import com.javaguard.ips.fiveg.session.FiveGSessionService;

import com.javaguard.ips.fiveg.statistics.FiveGTrafficStatisticsService;
import com.javaguard.ips.fiveg.statistics.FiveGTrafficStatisticsSnapshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(
        "/api/v1/5g/statistics"
)
public class FiveGStatisticsController {

    private final FiveGTrafficStatisticsService
            statisticsService;

    private final FiveGSessionService
            sessionService;


    public FiveGStatisticsController(
            FiveGTrafficStatisticsService statisticsService,
            FiveGSessionService sessionService
    ) {

        this.statisticsService =
                statisticsService;

        this.sessionService =
                sessionService;
    }


    @GetMapping
    public FiveGStatisticsResponse statistics() {

        return new FiveGStatisticsResponse(

                sessionService.count(),

                statisticsService.snapshot()
        );
    }


    public record FiveGStatisticsResponse(

            int activePduSessions,

            FiveGTrafficStatisticsSnapshot traffic

    ) {
    }
}