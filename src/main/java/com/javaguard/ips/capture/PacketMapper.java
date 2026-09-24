package com.javaguard.ips.capture;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV6CommonPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;


import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PacketMapper {
    public Optional<NetworkEvent>map(Packet packet){
        String sourceIp;
        String destinationIp;

        IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);

        IpV6Packet ipv6Packet = packet.get(IpV6Packet.class);

        if (ipv4Packet != null){
            sourceIp = ipv4Packet
                    .getHeader()
                    .getSrcAddr()
                    .getHostAddress();
            destinationIp = ipv4Packet
                    .getHeader()
                    .getDstAddr()
                    .getHostAddress();
        }else if (ipv6Packet != null) {

            sourceIp = ipv6Packet
                    .getHeader()
                    .getSrcAddr()
                    .getHostAddress();

            destinationIp = ipv6Packet
                    .getHeader()
                    .getDstAddr()
                    .getHostAddress();
        }else{
            return Optional.empty();
        }

        Integer sourcePort = null;
        Integer destinationPort = null;

        NetworkProtocol protocol = NetworkProtocol.OTHER;

        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        UdpPacket udpPacket = packet.get(UdpPacket.class);

        if(tcpPacket != null){
            sourcePort = tcpPacket
                    .getHeader()
                    .getSrcPort()
                    .valueAsInt();

            destinationPort = udpPacket
                    .getHeader()
                    .getDstPort()
                    .valueAsInt();

            protocol  = NetworkProtocol.TCP;

        } else if (udpPacket != null) {
            sourcePort = udpPacket
                    .getHeader()
                    .getSrcPort()
                    .valueAsInt();

            destinationPort = udpPacket
                    .getHeader()
                    .getDstPort()
                    .valueAsInt();

            protocol = NetworkProtocol.UDP;

        } else if (
                packet.get(IcmpV4CommonPacket.class) != null
        ) {

            protocol = NetworkProtocol.ICMP;

        } else if (
                packet.get(IcmpV6CommonPacket.class) != null
        ) {

            protocol = NetworkProtocol.ICMPV6;
        }

        NetworkEvent event =
                new NetworkEvent(
                        Instant.now(),
                        sourceIp,
                        destinationIp,
                        sourcePort,
                        destinationPort,
                        protocol,
                        packet.length()
                );

        return Optional.of(event);
    }
}