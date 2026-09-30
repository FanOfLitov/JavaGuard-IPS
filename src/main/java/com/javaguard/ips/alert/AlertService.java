package com.javaguard.ips.alert;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.persistence.SecurityAlertStore;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AlertService {

    private static final int MAX_ALERTS = 1_000;

    private final ConcurrentLinkedDeque<SecurityEvent> alerts =
            new ConcurrentLinkedDeque<>();

    private final SecurityAlertStore alertStore;


    public AlertService(
            SecurityAlertStore alertStore
    ) {

        this.alertStore = alertStore;
    }


    public void publish(
            SecurityEvent event
    ) {

        alerts.addFirst(event);

        while (alerts.size() > MAX_ALERTS) {
            alerts.pollLast();
        }

        alertStore.save(event);
    }


    public List<SecurityEvent> getRecentAlerts() {

        return alerts
                .stream()
                .limit(100)
                .toList();
    }


    public List<SecurityEvent> getAlertHistory() {

        return alertStore.findRecent();
    }


    public long getStoredAlertCount() {

        return alertStore.count();
    }
}