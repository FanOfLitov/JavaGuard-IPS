package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.reconciliation.FiveGReconciliationSnapshot;

import java.time.Instant;

public record Open5GsSyncStatus(
        boolean enabled,
        String smfBaseUrl,
        Instant lastAttemptAt,
        Instant lastSuccessAt,
        int controlPlaneSessions,
        String lastError,
        FiveGReconciliationSnapshot reconciliation
) {
}