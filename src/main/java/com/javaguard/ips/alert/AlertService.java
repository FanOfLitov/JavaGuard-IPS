package com.javaguard.ips.alert;
import com.javaguard.ips.detection.model.SecurityEvent;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AlertService {
    private static final int MAX_ALERTS =1_000;
    private final ConcurrentLinkedDeque<SecurityEvent>alerts=new ConcurrentLinkedDeque<>();

    public void publish(
            SecurityEvent event
    ){
        alerts.addFirst(event);

        while(alerts.size() > MAX_ALERTS){
            alerts.pollLast();
        }
    }

    public List<SecurityEvent> getRecentAlerts(){

        return alerts
                .stream()
                .limit(100)
                .toList();
    }
}
