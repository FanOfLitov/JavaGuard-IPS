package com.javaguard.ips.fiveg.reconciliation;

import com.javaguard.ips.alert.AlertService;
import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;

import com.javaguard.ips.fiveg.model.PduSessionContext;

import com.javaguard.ips.fiveg.open5gs.ControlPlaneSessionRegistry;
import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;

import com.javaguard.ips.fiveg.session.FiveGSessionRegistry;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FiveGReconciliationService {

    private static final Duration ALERT_COOLDOWN =
            Duration.ofMinutes(1);

    private final ControlPlaneSessionRegistry
            controlPlaneRegistry;

    private final FiveGSessionRegistry
            dataPlaneRegistry;

    private final AlertService
            alertService;

    private final ConcurrentMap<String, Instant> lastAlerts =
            new ConcurrentHashMap<>();


    public FiveGReconciliationService(
            ControlPlaneSessionRegistry controlPlaneRegistry,
            FiveGSessionRegistry dataPlaneRegistry,
            AlertService alertService
    ) {
        this.controlPlaneRegistry =
                controlPlaneRegistry;

        this.dataPlaneRegistry =
                dataPlaneRegistry;

        this.alertService =
                alertService;
    }


    public FiveGReconciliationSnapshot reconcile() {

        List<ControlPlanePduSession> controlSessions =
                controlPlaneRegistry.findAll();

        List<PduSessionContext> dataSessions =
                dataPlaneRegistry.findAll();


        Map<String, ControlPlanePduSession> controlByKey =
                new HashMap<>();

        for (ControlPlanePduSession session : controlSessions) {
            controlByKey.put(
                    session.sessionKey(),
                    session
            );
        }


        Map<String, PduSessionContext> dataByKey =
                new HashMap<>();

        for (PduSessionContext session : dataSessions) {
            dataByKey.put(
                    sessionKey(
                            session.supi(),
                            session.pduSessionId()
                    ),
                    session
            );
        }


        List<FiveGReconciliationSnapshot.Issue> issues =
                new ArrayList<>();

        Set<String> processedKeys =
                new HashSet<>();


        int matched = 0;
        int controlOnly = 0;
        int dataOnly = 0;
        int mismatched = 0;


        for (
                Map.Entry<String, ControlPlanePduSession> entry :
                controlByKey.entrySet()
        ) {

            String key =
                    entry.getKey();

            ControlPlanePduSession control =
                    entry.getValue();

            PduSessionContext data =
                    dataByKey.get(key);

            processedKeys.add(key);


            if (data == null) {

                controlOnly++;

                issues.add(
                        new FiveGReconciliationSnapshot.Issue(
                                FiveGReconciliationSnapshot
                                        .IssueType
                                        .CONTROL_PLANE_ONLY,
                                control.supi(),
                                control.pduSessionId(),
                                "Session exists in Open5GS SMF but JavaGuard has no data-plane context"
                        )
                );

                continue;
            }


            List<String> differences =
                    findDifferences(
                            control,
                            data
                    );


            if (differences.isEmpty()) {
                matched++;
                continue;
            }


            mismatched++;

            String message =
                    String.join(
                            "; ",
                            differences
                    );


            issues.add(
                    new FiveGReconciliationSnapshot.Issue(
                            FiveGReconciliationSnapshot
                                    .IssueType
                                    .CONTEXT_MISMATCH,
                            control.supi(),
                            control.pduSessionId(),
                            message
                    )
            );


            publishMismatchAlert(
                    control,
                    data,
                    message
            );
        }


        for (
                Map.Entry<String, PduSessionContext> entry :
                dataByKey.entrySet()
        ) {

            if (processedKeys.contains(entry.getKey())) {
                continue;
            }

            PduSessionContext data =
                    entry.getValue();

            dataOnly++;

            issues.add(
                    new FiveGReconciliationSnapshot.Issue(
                            FiveGReconciliationSnapshot
                                    .IssueType
                                    .DATA_PLANE_ONLY,
                            data.supi(),
                            data.pduSessionId(),
                            "JavaGuard has a PDU session that is absent from the Open5GS SMF snapshot"
                    )
            );


            publishStaleSessionAlert(
                    data
            );
        }


        return new FiveGReconciliationSnapshot(
                controlSessions.size(),
                dataSessions.size(),
                matched,
                controlOnly,
                dataOnly,
                mismatched,
                List.copyOf(issues),
                Instant.now()
        );
    }


    private List<String> findDifferences(
            ControlPlanePduSession control,
            PduSessionContext data
    ) {

        List<String> differences =
                new ArrayList<>();


        if (!control.ueIp().equals(data.ueIp())) {
            differences.add(
                    "UE IP mismatch: Open5GS="
                            + control.ueIp()
                            + ", JavaGuard="
                            + data.ueIp()
            );
        }


        if (!control.dnn().equalsIgnoreCase(data.dnn())) {
            differences.add(
                    "DNN mismatch: Open5GS="
                            + control.dnn()
                            + ", JavaGuard="
                            + data.dnn()
            );
        }


        if (!control.slice().equals(data.slice())) {
            differences.add(
                    "S-NSSAI mismatch: Open5GS="
                            + control.slice().displayName()
                            + ", JavaGuard="
                            + data.slice().displayName()
            );
        }


        return differences;
    }


    private void publishStaleSessionAlert(
            PduSessionContext data
    ) {

        String key =
                "STALE#"
                        + data.supi()
                        + "#"
                        + data.pduSessionId();

        Instant now =
                Instant.now();

        if (isInCooldown(key, now)) {
            return;
        }

        lastAlerts.put(
                key,
                now
        );


        SecurityEvent event =
                new SecurityEvent(
                        UUID.randomUUID(),
                        now,
                        ThreatType.STALE_PDU_SESSION,
                        Severity.MEDIUM,
                        data.ueIp(),
                        data.upfIp(),
                        "PDU session exists in JavaGuard but is absent from Open5GS SMF: SUPI="
                                + data.supi()
                                + ", PDU session="
                                + data.pduSessionId(),
                        1
                );

        alertService.publish(event);
    }


    private void publishMismatchAlert(
            ControlPlanePduSession control,
            PduSessionContext data,
            String differences
    ) {

        String key =
                "MISMATCH#"
                        + data.supi()
                        + "#"
                        + data.pduSessionId();

        Instant now =
                Instant.now();

        if (isInCooldown(key, now)) {
            return;
        }

        lastAlerts.put(
                key,
                now
        );


        SecurityEvent event =
                new SecurityEvent(
                        UUID.randomUUID(),
                        now,
                        ThreatType.CONTROL_PLANE_CONTEXT_MISMATCH,
                        Severity.HIGH,
                        data.ueIp(),
                        data.upfIp(),
                        "Open5GS/JavaGuard context mismatch: "
                                + differences
                                + "; SUPI="
                                + control.supi()
                                + "; PDU session="
                                + control.pduSessionId(),
                        differences.split(";").length
                );

        alertService.publish(event);
    }


    private boolean isInCooldown(
            String key,
            Instant now
    ) {

        Instant previous =
                lastAlerts.get(key);

        return previous != null
                && now.isBefore(
                previous.plus(
                        ALERT_COOLDOWN
                )
        );
    }


    private String sessionKey(
            String supi,
            int pduSessionId
    ) {
        return supi + "#" + pduSessionId;
    }
}