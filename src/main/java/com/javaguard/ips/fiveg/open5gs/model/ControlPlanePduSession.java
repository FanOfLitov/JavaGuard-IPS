package com.javaguard.ips.fiveg.open5gs.model;

import com.javaguard.ips.fiveg.model.SliceId;

import java.time.Instant;

public record ControlPlanePduSession(
        String supi,
        int pduSessionId,
        String ueIp,
        String dnn,
        SliceId slice,
        String pduState,
        String ueActivity,
        Instant observedAt
) {

    public String sessionKey() {
        return supi + "#" + pduSessionId;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(pduState);
    }
}