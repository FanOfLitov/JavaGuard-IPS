package com.javaguard.ips.event;

import java.time.Instant;

public record NetworkEvent (
    Instant timestamp,
    String sourceIp,
    String destinationIp,
    Integer sourcePort,
    Integer destinationPort,
    NetworkProtocol protocol,
    int packetSize,
    boolean tcpSyn,
    boolean tcpAck,
    boolean tcpRst,
    boolean tcpFin
){
}
