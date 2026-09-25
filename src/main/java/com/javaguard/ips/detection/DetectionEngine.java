package com.javaguard.ips.detection;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.event.NetworkEvent;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetectionEngine {
    private final List<DetectionRule>rules;
    private final AlertService alertService;


    public DetectionEngine(
            List<DetectionRule> rules,
            AlertService alertService
    ){
        this.rules = rules;
        this.alertService = alertService;
    }

    public void analyze(NetworkEvent event) {

        rules
                .stream()
                .map(
                        rule ->
                                rule.analyze(event)
                )
                .flatMap(Optional::stream)
                .forEach(
                        alertService::publish
                );
    }


}
