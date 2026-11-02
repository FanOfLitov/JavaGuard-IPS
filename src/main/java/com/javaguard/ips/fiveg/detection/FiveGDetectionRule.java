package com.javaguard.ips.fiveg.detection;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;

import java.util.Optional;


public interface FiveGDetectionRule {

    Optional<SecurityEvent> analyze(
            EnrichedNetworkEvent event
    );
}