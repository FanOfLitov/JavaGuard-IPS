package com.javaguard.ips.detection.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class PortScanWindow {

    private final Map<Integer, Instant> ports = new HashMap<>();

    public synchronized int record(
            int port,
            Instant timestamp,
            Duration window
    ){
        Instant cutoff = timestamp.minus(window);


        ports
                .entrySet()
                .removeIf(
                        entry ->
                                entry
                                        .getValue()
                                       .isBefore(cutoff)
                );
        ports.put(
                port,
                timestamp
        );

        return ports.size();
    }
    public synchronized void clear(){
        ports.clear();
    }
}
