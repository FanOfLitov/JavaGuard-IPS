package com.javaguard.ips.fiveg.reconciliation;

import java.time.Instant;
import java.util.List;

public record FiveGReconciliationSnapshot(
        int controlPlaneSessions,
        int dataPlaneSessions,
        int matchedSessions,
        int controlPlaneOnlySessions,
        int dataPlaneOnlySessions,
        int mismatchedSessions,
        List<Issue> issues,
        Instant generatedAt
) {

    public enum IssueType {
        CONTROL_PLANE_ONLY,
        DATA_PLANE_ONLY,
        CONTEXT_MISMATCH
    }


    public record Issue(
            IssueType type,
            String supi,
            int pduSessionId,
            String message
    ) {
    }
}