package com.javaguard.ips.detection.support;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DistinctSlidingWindow {

    private final ConcurrentMap<String, Window> windows =
            new ConcurrentHashMap<>();


    public int record(
            String key,
            String value,
            Instant timestamp,
            Duration duration
    ) {

        Window window =
                windows.computeIfAbsent(
                        key,
                        ignored -> new Window()
                );

        return window.record(
                value,
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

        private final Deque<Observation> observations =
                new ArrayDeque<>();

        private Instant lastSeen;


        private synchronized int record(
                String value,
                Instant timestamp,
                Duration duration
        ) {

            lastSeen = timestamp;

            Instant cutoff =
                    timestamp.minus(duration);

            while (!observations.isEmpty()
                    && observations
                    .peekFirst()
                    .timestamp()
                    .isBefore(cutoff)) {

                observations.removeFirst();
            }

            observations.addLast(
                    new Observation(
                            timestamp,
                            value
                    )
            );

            Set<String> unique =
                    new HashSet<>();

            for (Observation observation : observations) {
                unique.add(
                        observation.value()
                );
            }

            return unique.size();
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


    private record Observation(
            Instant timestamp,
            String value
    ) {
    }
}