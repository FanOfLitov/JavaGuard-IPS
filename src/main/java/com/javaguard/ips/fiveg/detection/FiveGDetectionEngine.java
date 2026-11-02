package com.javaguard.ips.fiveg.detection;

import com.javaguard.ips.alert.AlertService;

import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FiveGDetectionEngine {

    private final List<FiveGDetectionRule>
            rules;

    private final AlertService
            alertService;


    public FiveGDetectionEngine(
            List<FiveGDetectionRule> rules,
            AlertService alertService
    ) {

        this.rules =
                rules;

        this.alertService =
                alertService;
    }


    public void analyze(
            EnrichedNetworkEvent event
    ) {

        for (
                FiveGDetectionRule rule :
                rules
        ) {

            rule
                    .analyze(event)
                    .ifPresent(
                            alertService::publish
                    );
        }
    }
}