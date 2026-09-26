package com.javaguard.ips.detection.rule;
import com.javaguard.ips.detection.DetectionRule;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PortScanRule implements DetectionRule{
    private static final int PORT_THRESHOLD=15;

    private static final Duration WINDOW =Duration.ofSeconds(10);

    private static final Duration ALERT_COOLDOWN = Duration.ofSeconds(30);

    private final Map<ScanKey,PortScanWindow> windows= new ConcurrentHashMap<>();

    private final Map<ScanKey, Instant> lastAlerts = new ConcurrentHashMap<>();

    @Override
    public Optional<SecurityEvent> analyze(NetworkEvent event){
        if(event.protocol() != NetworkProtocol.TCP){
            return Optional.empty();
        }
        if(event.destinationPort() == null){
            return Optional.empty();
        }
        if(!event.tcpSyn() || event.tcpAck()){
            return Optional.empty();
        }

        ScanKey key = new ScanKey(
                event.sourceIp(),
                event.destinationIp()
        );

        PortScanWindow window = windows.computeIfAbsent(
                key,
                ignored -> new PortScanWindow()

        );
        int distinctPorts =
                window.record(
                        event.destinationPort(),
                        event.timestamp(),
                        WINDOW
                );

        if(distinctPorts <PORT_THRESHOLD){
            return Optional.empty();
        }

        Instant lastAlert =  lastAlerts.get(key);

        if(
                lastAlert != null && Duration
                        .between(
                                lastAlert,
                                event.timestamp()
                        )
                        .compareTo(ALERT_COOLDOWN) <0
        ){
            return Optional.empty();
        }

        lastAlerts.put(
                key,
                event.timestamp()
        );

        window.clear();

        SecurityEvent securityEvent =
                new SecurityEvent(
                        UUID.randomUUID(),
                        event.timestamp(),
                        ThreatType.PORT_SCAN,
                        Severity.HIGH,
                        event.sourceIp(),
                        event.destinationIp(),
                        "TCP port scan detected: "
                            +distinctPorts
                            +"distinct ports withis "
                            + WINDOW.toSeconds()
                            + " seconds",
                        distinctPorts

                );

        return Optional.of(
                securityEvent
        );
    }

    private record ScanKey(
            String sourceIp,
            String destinationIp
    ){

    }
}
