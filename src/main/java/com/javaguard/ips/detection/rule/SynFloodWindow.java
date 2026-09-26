package com.javaguard.ips.detection.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class SynFloodWindow {
    private final Deque<Instant> synPackets = new ArrayDeque<>();

    public synchronized int record(
            Instant timestamp,
            Duration window
    ){
        Instant cutoff = timestamp.minus(window);

        while(
                !synPackets.isEmpty() && synPackets
                        .peekFirst()
                        .isBefore(cutoff)
        ){
            synPackets.removeFirst();
        }

        synPackets.addLast(timestamp);
        return synPackets.size();
    }

    public synchronized void clear(){
        synPackets.clear();
    }
}
