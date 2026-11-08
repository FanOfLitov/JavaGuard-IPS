package com.javaguard.ips.flow.tcp;

public record TcpFlowSummary(
        int trackedFlows,
        int halfOpenFlows,
        int establishedFlows,
        int closingFlows,
        int closedFlows,
        int resetFlows
) {
}