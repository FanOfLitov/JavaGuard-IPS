package com.javaguard.ips.fiveg.open5gs.model;

import com.javaguard.ips.fiveg.model.SliceId;

import java.time.Instant;
import java.util.List;

public record AmfUeContext(
        String supi,
        String connectionState,
        Long gnbId,
        Long cellId,
        List<SliceId> requestedSlices,
        List<SliceId> allowedSlices,
        Instant observedAt
) {

    public boolean isConnected() {
        return "connected".equalsIgnoreCase(connectionState);
    }

    public boolean allowsSlice(SliceId slice) {
        return allowedSlices.contains(slice);
    }
}