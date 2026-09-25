package com.javaguard.ips.detection.model;

import java.time.Instant;
import java.util.UUID;

public record SecurityEvent (
    UUID id,
    Instant timestamp,
    ThreatType type,
    Severity severity,
    String sourceIp,
    String destinationIp,
    String description,
    int evidenceCount
    ){}
