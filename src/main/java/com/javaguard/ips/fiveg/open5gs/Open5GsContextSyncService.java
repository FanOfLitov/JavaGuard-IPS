package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;

import com.javaguard.ips.fiveg.reconciliation.FiveGReconciliationService;
import com.javaguard.ips.fiveg.reconciliation.FiveGReconciliationSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;

@Service
public class Open5GsContextSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    Open5GsContextSyncService.class
            );

    private final Open5GsInfoProperties properties;

    private final Open5GsInfoClient client;

    private final ControlPlaneSessionRegistry
            controlPlaneRegistry;

    private final FiveGReconciliationService
            reconciliationService;


    private volatile Instant lastAttemptAt;

    private volatile Instant lastSuccessAt;

    private volatile String lastError;

    private volatile FiveGReconciliationSnapshot
            lastReconciliation;


    public Open5GsContextSyncService(
            Open5GsInfoProperties properties,
            Open5GsInfoClient client,
            ControlPlaneSessionRegistry controlPlaneRegistry,
            FiveGReconciliationService reconciliationService
    ) {
        this.properties = properties;
        this.client = client;
        this.controlPlaneRegistry =
                controlPlaneRegistry;
        this.reconciliationService =
                reconciliationService;
    }


    @Scheduled(
            fixedDelayString =
                    "${javaguard.fiveg.open5gs.sync-interval-ms:5000}"
    )
    public void scheduledSync() {

        if (!properties.isEnabled()) {
            return;
        }

        syncNow();
    }


    public synchronized Open5GsSyncStatus syncNow() {

        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "Open5GS synchronization is disabled"
            );
        }

        lastAttemptAt =
                Instant.now();

        try {
            List<ControlPlanePduSession> sessions =
                    client.fetchPduSessions();

            controlPlaneRegistry
                    .replaceAll(sessions);

            lastReconciliation =
                    reconciliationService
                            .reconcile();

            lastSuccessAt =
                    Instant.now();

            lastError =
                    null;

            log.info(
                    "Open5GS context synchronized: sessions={}, matched={}, mismatched={}",
                    sessions.size(),
                    lastReconciliation.matchedSessions(),
                    lastReconciliation.mismatchedSessions()
            );

        } catch (RuntimeException exception) {

            lastError =
                    exception.getMessage();

            log.warn(
                    "Open5GS context synchronization failed",
                    exception
            );
        }

        return status();
    }


    public Open5GsSyncStatus status() {

        return new Open5GsSyncStatus(
                properties.isEnabled(),
                properties.getSmfBaseUrl(),
                lastAttemptAt,
                lastSuccessAt,
                controlPlaneRegistry.size(),
                lastError,
                lastReconciliation
        );
    }


    public FiveGReconciliationSnapshot
    getLastReconciliation() {

        return lastReconciliation;
    }
}