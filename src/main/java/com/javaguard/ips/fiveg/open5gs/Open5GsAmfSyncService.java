package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;
import com.javaguard.ips.fiveg.open5gs.model.GnbContext;

import com.javaguard.ips.fiveg.reconciliation
        .FiveGControlPlaneConsistencyService;

import com.javaguard.ips.fiveg.reconciliation
        .FiveGControlPlaneConsistencySnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;

@Service
public class Open5GsAmfSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    Open5GsAmfSyncService.class
            );

    private final Open5GsInfoProperties properties;
    private final Open5GsAmfInfoClient client;

    private final AmfUeRegistry ueRegistry;
    private final GnbRegistry gnbRegistry;

    private final FiveGControlPlaneConsistencyService
            consistencyService;


    private volatile Instant lastAttemptAt;
    private volatile Instant lastSuccessAt;

    private volatile String lastError;

    private volatile FiveGControlPlaneConsistencySnapshot
            lastConsistency;


    public Open5GsAmfSyncService(
            Open5GsInfoProperties properties,
            Open5GsAmfInfoClient client,
            AmfUeRegistry ueRegistry,
            GnbRegistry gnbRegistry,
            FiveGControlPlaneConsistencyService consistencyService
    ) {
        this.properties = properties;
        this.client = client;
        this.ueRegistry = ueRegistry;
        this.gnbRegistry = gnbRegistry;
        this.consistencyService = consistencyService;
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


    public synchronized Open5GsAmfSyncStatus syncNow() {

        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "Open5GS synchronization is disabled"
            );
        }

        lastAttemptAt =
                Instant.now();

        try {
            /*
             * Сначала получаем оба snapshot.
             *
             * Registry меняем только если оба запроса
             * успешно завершились.
             */
            List<AmfUeContext> ues =
                    client.fetchUes();

            List<GnbContext> gnbs =
                    client.fetchGnbs();


            ueRegistry.replaceAll(ues);
            gnbRegistry.replaceAll(gnbs);


            lastConsistency =
                    consistencyService.analyze();

            lastSuccessAt =
                    Instant.now();

            lastError =
                    null;


            log.info(
                    "Open5GS AMF synchronized: ues={}, gnbs={}, issues={}",
                    ues.size(),
                    gnbs.size(),
                    lastConsistency.issues().size()
            );

        } catch (RuntimeException exception) {

            lastError =
                    exception.getMessage();

            log.warn(
                    "Open5GS AMF synchronization failed",
                    exception
            );
        }


        return status();
    }


    public Open5GsAmfSyncStatus status() {

        return new Open5GsAmfSyncStatus(
                properties.isEnabled(),
                properties.getAmfBaseUrl(),
                lastAttemptAt,
                lastSuccessAt,
                ueRegistry.size(),
                gnbRegistry.size(),
                lastError,
                lastConsistency
        );
    }


    public FiveGControlPlaneConsistencySnapshot
    getLastConsistency() {

        return lastConsistency;
    }
}