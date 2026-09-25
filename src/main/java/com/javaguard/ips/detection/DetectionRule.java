package com.javaguard.ips.detection;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.event.NetworkEvent;

import java.util.Optional;

public interface DetectionRule {
    Optional<SecurityEvent> analyze(
            NetworkEvent event
    );
}
