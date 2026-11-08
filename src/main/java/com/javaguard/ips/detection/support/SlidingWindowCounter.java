package com.javaguard.ips.detection.support;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayDeque;
import java.util.Deque;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SlidingWindowCounter {

    private final ConcurrentMap<String, Window> windows =
            new ConcurrentHashMap<>();


    public int record(
            String key,
            Instant timestamp,
            Duration duration
    ) {

        Window window =
                windows.computeIfAbsent(
                        key,
                        ignored -> new Window()
                );

        return window.record(
                timestamp,
                duration
        );
    }


    public void cleanup(
            Instant now,
            Duration maxIdle
    ) {

        windows.entrySet().removeIf(
                entry ->
                        entry
                                .getValue()
                                .isIdle(
                                        now,
                                        maxIdle
                                )
        );
    }


    private static class Window {

        private final Deque<Instant> timestamps =
                new ArrayDeque<>();

        private Instant lastSeen;


        private synchronized int record(
                Instant timestamp,
                Duration duration
        ) {

            lastSeen = timestamp;

            Instant cutoff =
                    timestamp.minus(duration);

            while (!timestamps.isEmpty()
                    && timestamps.peekFirst().isBefore(cutoff)) {

                timestamps.removeFirst();
            }

            timestamps.addLast(timestamp);

            return timestamps.size();
        }


        private synchronized boolean isIdle(
                Instant now,
                Duration maxIdle
        ) {

            if (lastSeen == null) {
                return true;
            }

            return lastSeen
                    .plus(maxIdle)
                    .isBefore(now);
        }
    }
}