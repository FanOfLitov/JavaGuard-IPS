package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.reconciliation
        .FiveGControlPlaneConsistencySnapshot;

import java.time.Instant;

public record Open5GsAmfSyncStatus(
        boolean enabled,
        String amfBaseUrl,
        Instant lastAttemptAt,
        Instant lastSuccessAt,
        int ueContexts,
        int gnbContexts,
        String lastError,
        FiveGControlPlaneConsistencySnapshot consistency
) {
}