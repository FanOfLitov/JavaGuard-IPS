package com.javaguard.ips.flow.tcp;

import java.time.Instant;
import java.util.UUID;

public record TcpFlowSnapshot(
        UUID id,
        TcpFlowKey key,
        TcpFlowState state,
        Instant createdAt,
        Instant lastSeenAt,
        Instant establishedAt,
        Instant closedAt,
        long packets,
        long bytes,
        long clientPackets,
        long serverPackets,
        long synPackets,
        long rstPackets,
        long finPackets
) {

    public boolean isHalfOpen() {
        return state == TcpFlowState.SYN_SENT
                || state == TcpFlowState.SYN_ACK_SEEN;
    }


    public boolean isEstablished() {
        return state == TcpFlowState.ESTABLISHED
                || state == TcpFlowState.CLOSING
                || state == TcpFlowState.CLOSED;
    }


    public boolean isTerminal() {
        return state == TcpFlowState.CLOSED
                || state == TcpFlowState.RESET;
    }
}