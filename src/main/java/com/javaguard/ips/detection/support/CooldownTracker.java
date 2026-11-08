package com.javaguard.ips.detection.support;

import java.time.Duration;
import java.time.Instant;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CooldownTracker {

    private final ConcurrentMap<String, Instant> lastAlerts =
            new ConcurrentHashMap<>();


    public boolean tryAcquire(
            String key,
            Instant now,
            Duration cooldown
    ) {

        while (true) {

            Instant previous =
                    lastAlerts.get(key);

            if (previous != null
                    && now.isBefore(
                    previous.plus(cooldown)
            )) {
                return false;
            }

            if (previous == null) {

                if (lastAlerts.putIfAbsent(
                        key,
                        now
                ) == null) {
                    return true;
                }

                continue;
            }

            if (lastAlerts.replace(
                    key,
                    previous,
                    now
            )) {
                return true;
            }
        }
    }


    public void cleanup(
            Instant now,
            Duration maxAge
    ) {

        lastAlerts.entrySet().removeIf(
                entry ->
                        entry
                                .getValue()
                                .plus(maxAge)
                                .isBefore(now)
        );
    }
}