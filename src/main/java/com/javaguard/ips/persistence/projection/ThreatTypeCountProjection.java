package com.javaguard.ips.persistence.projection;


import com.javaguard.ips.detection.model.ThreatType;

public interface ThreatTypeCountProjection {

    ThreatType getType();

    long getCount();
}
