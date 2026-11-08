package com.javaguard.ips.flow.tcp;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpFlowTrackerTest {

    @Test
    void shouldTrackCompleteTcpHandshake() {

        TcpFlowTracker tracker =
                new TcpFlowTracker();

        Instant now =
                Instant.now();


        NetworkEvent syn =
                event(
                        now,
                        "10.0.0.10",
                        50000,
                        "10.0.0.20",
                        443,
                        true,
                        false,
                        false,
                        false
                );


        TcpFlowSnapshot afterSyn =
                tracker
                        .track(syn)
                        .orElseThrow();


        assertEquals(
                TcpFlowState.SYN_SENT,
                afterSyn.state()
        );


        NetworkEvent synAck =
                event(
                        now.plusMillis(10),
                        "10.0.0.20",
                        443,
                        "10.0.0.10",
                        50000,
                        true,
                        true,
                        false,
                        false
                );


        TcpFlowSnapshot afterSynAck =
                tracker
                        .track(synAck)
                        .orElseThrow();


        assertEquals(
                TcpFlowState.SYN_ACK_SEEN,
                afterSynAck.state()
        );


        NetworkEvent ack =
                event(
                        now.plusMillis(20),
                        "10.0.0.10",
                        50000,
                        "10.0.0.20",
                        443,
                        false,
                        true,
                        false,
                        false
                );


        TcpFlowSnapshot established =
                tracker
                        .track(ack)
                        .orElseThrow();


        assertEquals(
                TcpFlowState.ESTABLISHED,
                established.state()
        );

        assertTrue(
                established.isEstablished()
        );

        assertEquals(
                3,
                established.packets()
        );
    }


    private NetworkEvent event(
            Instant timestamp,
            String sourceIp,
            int sourcePort,
            String destinationIp,
            int destinationPort,
            boolean syn,
            boolean ack,
            boolean rst,
            boolean fin
    ) {

        return new NetworkEvent(
                timestamp,
                sourceIp,
                destinationIp,
                sourcePort,
                destinationPort,
                NetworkProtocol.TCP,
                60,
                syn,
                ack,
                rst,
                fin
        );
    }
}