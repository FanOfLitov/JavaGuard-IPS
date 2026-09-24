package com.javaguard.ips.capture;

import com.javaguard.ips.event.NetworkEvent;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import org.springframework.stereotype.Service;

import java.io.EOFException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class PacketCaptureService {

    private static final int SNAPSHOT_LENGTH = 65_536;
    private static final int READ_TIMEOUT_MILLIS = 1_000;
    private static final int MAX_CAPTURE_SECONDS =10;

    private final PacketMapper packetMapper;

    public PacketCaptureService(
            PacketMapper packetMapper
    ){
        this.packetMapper = packetMapper;
    }

    public List<NetworkEvent> capture(
            String interfaceName,
            int count
    ){
        if(count<1 || count >100){
            throw new IllegalArgumentException(
                    "Packet count must be between1 and 100"
            );
        }

        PcapHandle handle = null;

        try {

            PcapNetworkInterface networkInterface = Pcaps.getDevByName(interfaceName);

            if(networkInterface == null){
                throw new IllegalArgumentException(
                        "Network interface not found: "+interfaceName
                );
            }

            handle =networkInterface.openLive(
                    SNAPSHOT_LENGTH,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,READ_TIMEOUT_MILLIS

            );

            List <NetworkEvent> events = new ArrayList<>();

            Instant deadline = Instant.now().plusSeconds(MAX_CAPTURE_SECONDS);

            try {

                PcapNetworkInterface networkInterface =
                        Pcaps.getDevByName(interfaceName);

                if (networkInterface == null) {
                    throw new IllegalArgumentException(
                            "Network interface not found: "
                                    + interfaceName
                    );
                }

                handle = networkInterface.openLive(
                        SNAPSHOT_LENGTH,
                        PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                        READ_TIMEOUT_MILLIS
                );

                List<NetworkEvent> events =
                        new ArrayList<>();

                Instant deadline =
                        Instant.now()
                                .plusSeconds(MAX_CAPTURE_SECONDS);

                while (
                        events.size() < count
                                && Instant.now().isBefore(deadline)
                ) {

                    try {

                        Packet packet =
                                handle.getNextPacketEx();

                        packetMapper
                                .map(packet)
                                .ifPresent(events::add);

                    } catch (TimeoutException ignored) {

                        // No packet arrived during this read timeout.
                    }
                }

                return List.copyOf(events);

            } catch (
                    PcapNativeException
                    | NotOpenException
                    | EOFException exception
            ) {

                throw new IllegalStateException(
                        "Packet capture failed on interface: "
                                + interfaceName,
                        exception
                );

            } finally {

                if (handle != null && handle.isOpen()) {
                    handle.close();
                }
            }
