package com.javaguard.ips.flow.tcp;

public enum TcpFlowState {

    SYN_SENT,
    SYN_ACK_SEEN,
    ESTABLISHED,
    CLOSING,
    CLOSED,
    RESET
}