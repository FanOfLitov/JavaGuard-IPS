package com.javaguard.ips.fiveg.gtpu;

import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GtpUPacketInspector {

    public static final int GTP_U_PORT = 2152;

    private final GtpUParser parser;

    public GtpUPacketInspector(
            GtpUParser parser
    ) {
        this.parser = parser;
    }

    public Optional<GtpUPacketData> inspect(
            Packet packet
    ) {

        if (packet == null) {
            return Optional.empty();
        }

        UdpPacket udpPacket =
                packet.get(UdpPacket.class);

        if (udpPacket == null) {
            return Optional.empty();
        }

        int sourcePort = udpPacket
                        .getHeader()
                        .getSrcPort()
                        .valueAsInt();

        int destinationPort = udpPacket
                        .getHeader()
                        .getDstPort()
                        .valueAsInt();

        if (sourcePort != GTP_U_PORT
                && destinationPort != GTP_U_PORT) {

            return Optional.empty();
        }

        Packet payload = udpPacket.getPayload();

        if (payload == null) {
            return Optional.empty();
        }

        return parser.parse(payload.getRawData()
        );
    }
}