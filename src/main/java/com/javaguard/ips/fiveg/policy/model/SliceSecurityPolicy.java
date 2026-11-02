package com.javaguard.ips.fiveg.policy.model;

import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.model.SliceId;

import java.time.Instant;
import java.util.UUID;

public record SliceSecurityPolicy(
        UUID id,
        String name,
        SliceId slice,
        String dnn,
        boolean allowTcp,
        boolean allowUdp,
        boolean allowIcmp,
        boolean allowOther,
        int maxPacketsPerSecond,
        Severity severity,
        boolean enabled,
        Instant createdAt
) {

    public boolean matches(
            PduSessionContext context
    ) {

        return context.slice().equals(slice)
                && context.dnn().equalsIgnoreCase(dnn);
    }


    public boolean allowsProtocol(
            int protocol
    ) {

        return switch (protocol) {
            case 6 -> allowTcp;
            case 17 -> allowUdp;
            case 1, 58 -> allowIcmp;
            default -> allowOther;
        };
    }
}