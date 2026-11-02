package com.javaguard.ips.fiveg.open5gs.model;

import com.javaguard.ips.fiveg.model.SliceId;

import java.time.Instant;
import java.util.List;

public record GnbContext(
        long gnbId,
        String plmn,
        String sctpPeer,
        boolean setupSuccessful,
        int connectedUes,
        List<SliceId> supportedSlices,
        Instant observedAt
) {
}