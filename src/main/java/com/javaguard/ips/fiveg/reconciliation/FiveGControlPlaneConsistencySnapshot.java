package com.javaguard.ips.fiveg.reconciliation;

import java.time.Instant;
import java.util.List;

public record FiveGControlPlaneConsistencySnapshot(
        int amfUes,
        int smfSessions,
        int gnbs,
        int matchedSessions,
        int sessionsWithoutAmfRegistration,
        int uesWithUnknownGnb,
        int sliceMismatches,
        List<Issue> issues,
        Instant generatedAt
) {

    public enum IssueType {
        SESSION_WITHOUT_AMF_REGISTRATION,
        UNKNOWN_GNB,
        SLICE_MISMATCH
    }


    public record Issue(
            IssueType type,
            String supi,
            Integer pduSessionId,
            Long gnbId,
            String message
    ) {
    }
}