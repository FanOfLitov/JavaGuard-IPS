package com.javaguard.ips.persistence.projection;


import com.javaguard.ips.detection.model.Severity;

public interface SeverityCountProjection {

    Severity getSeverity();

    long getCount();
}
