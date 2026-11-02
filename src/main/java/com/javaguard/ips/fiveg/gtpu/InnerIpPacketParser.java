package com.javaguard.ips.fiveg.gtpu;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Optional;

@Component
public class InnerIpPacketParser {

    private static final int TCP_PROTOCOL = 6;
    private static final int UDP_PROTOCOL = 17;

    public Optional<InnerIpPacketInfo> parse(
            byte[] data
    ) {

        if (data == null || data.length == 0) {
            return Optional.empty();
        }

        int version = (Byte.toUnsignedInt(data[0]) >> 4)
                        & 0x0F;

        if (version == 4) {
            return parseIpv4(data);
        }

        if (version == 6) {
            return parseIpv6(data);
        }

        return Optional.empty();
    }

    private Optional<InnerIpPacketInfo> parseIpv4(byte[] data) {

        if (data.length < 20) {
            return Optional.empty();
        }

        int ihl = Byte.toUnsignedInt(data[0])
                        & 0x0F;

        int headerLength = ihl * 4;

        if (headerLength < 20 || data.length < headerLength) {

            return Optional.empty();
        }

        int totalLength = (Byte.toUnsignedInt(data[2]) << 8)
                        | Byte.toUnsignedInt(data[3]);

        if (totalLength < headerLength || totalLength > data.length) {

            return Optional.empty();
        }

        int protocol = Byte.toUnsignedInt(data[9]);

        String sourceIp = ipv4Address(
                        data,
                        12
                );

        String destinationIp = ipv4Address(
                        data,
                        16
                );

        Ports ports = parsePorts(
                        data,
                        headerLength,
                        protocol,
                        totalLength
                );

        return Optional.of(new InnerIpPacketInfo(
                        4,
                        sourceIp,
                        destinationIp,
                        ports.sourcePort(),
                        ports.destinationPort(),
                        protocol,
                        totalLength
                )
        );
    }

    private Optional<InnerIpPacketInfo> parseIpv6(
            byte[] data
    ) {

        if (data.length < 40) {
            return Optional.empty();
        }

        int payloadLength =
                (Byte.toUnsignedInt(data[4]) << 8)
                        | Byte.toUnsignedInt(data[5]);

        int totalLength = 40 + payloadLength;

        if (totalLength > data.length) {
            return Optional.empty();
        }

        int nextHeader = Byte.toUnsignedInt(data[6]);

        String sourceIp = ipv6Address(
                        data,
                        8
                );

        String destinationIp =
                ipv6Address(
                        data,
                        24
                );


        Ports ports = parsePorts(
                        data,
                        40,
                        nextHeader,
                        totalLength
                );

        return Optional.of(new InnerIpPacketInfo(
                        6,
                        sourceIp,
                        destinationIp,
                        ports.sourcePort(),
                        ports.destinationPort(),
                        nextHeader,
                        totalLength
                )
        );
    }

    private Ports parsePorts(
            byte[] data,
            int transportOffset,
            int protocol,
            int packetEnd
    ) {

        if (protocol != TCP_PROTOCOL && protocol != UDP_PROTOCOL) {

            return Ports.empty();
        }

        if (transportOffset + 4 > packetEnd) {
            return Ports.empty();
        }

        int sourcePort = (Byte.toUnsignedInt(data[transportOffset]) << 8)
                        | Byte.toUnsignedInt(
                        data[transportOffset + 1]
                );

        int destinationPort = (Byte.toUnsignedInt(data[transportOffset + 2]) << 8)
                        | Byte.toUnsignedInt(
                        data[transportOffset + 3]
                );

        return new Ports(sourcePort, destinationPort
        );
    }

    private String ipv4Address(
            byte[] data,
            int offset
    ) {

        return Byte.toUnsignedInt(data[offset])
                + "."
                + Byte.toUnsignedInt(data[offset + 1])
                + "."
                + Byte.toUnsignedInt(data[offset + 2])
                + "."
                + Byte.toUnsignedInt(data[offset + 3]);
    }

    private String ipv6Address(byte[] data, int offset) {

        byte[] address =
                new byte[16];

        System.arraycopy(
                data,
                offset,
                address,
                0,
                16
        );

        try {
            return InetAddress
                    .getByAddress(address)
                    .getHostAddress();

        } catch (UnknownHostException exception) {
            throw new IllegalStateException(
                    "Failed to parse IPv6 address",
                    exception
            );
        }
    }

    private record Ports(
            Integer sourcePort,
            Integer destinationPort
    ) {

        private static Ports empty() {
            return new Ports(
                    null,
                    null
            );
        }
    }
}