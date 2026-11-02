package com.javaguard.ips.web.dto;

import com.javaguard.ips.detection.model.Severity;

public record CreateSlicePolicyRequest(
        String name,
        int sst,
        String sd,
        String dnn,
        boolean allowTcp,
        boolean allowUdp,
        boolean allowIcmp,
        boolean allowOther,
        int maxPacketsPerSecond,
        Severity severity
) {
}