package com.javaguard.ips.flow.tcp;

public record TcpClientFlowStats(
        String clientIp,
        int totalFlows,
        int establishedFlows,
        int halfOpenFlows,
        int resetFlows,
        int closedFlows,
        double failureRatio
) {
}