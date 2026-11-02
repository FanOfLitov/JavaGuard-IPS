package com.javaguard.ips.fiveg.gtpu;

public record InnerIpPacketInfo(
        int ipVersion,
        String sourceIp,
        String destinationIp,
        Integer sourcePort,
        Integer destinationPort,
        int protocol,
        int packetLength
) {
}