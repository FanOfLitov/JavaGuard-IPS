package com.javaguard.ips.flow.tcp;

public record TcpFlowKey(
        String clientIp,
        int clientPort,
        String serverIp,
        int serverPort
) {

    public String displayName() {
        return clientIp
                + ":"
                + clientPort
                + " -> "
                + serverIp
                + ":"
                + serverPort;
    }
}