package com.javaguard.ips.capture;

import com.javaguard.ips.capture.model.CapturedTrafficEvent;
import com.javaguard.ips.detection.DetectionEngine;
import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;
import com.javaguard.ips.fiveg.correlation.FiveGEnrichmentService;
import com.javaguard.ips.fiveg.detection.FiveGDetectionEngine;
import com.javaguard.ips.fiveg.gtpu.GtpUHeader;
import com.javaguard.ips.fiveg.gtpu.GtpUPacketInspector;
import com.javaguard.ips.fiveg.statistics.FiveGTrafficStatisticsService;
import com.javaguard.ips.statistics.TrafficStatisticsService;
import com.javaguard.ips.flow.tcp.TcpFlowTracker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.io.EOFException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.javaguard.ips.fiveg.gtpu.GtpUHeader;

@Service
public class CapturePipelineService {

    private static final Logger log =
            LoggerFactory.getLogger(CapturePipelineService.class);

    private static final int SNAPSHOT_LENGTH = 65_536;
    private static final int READ_TIMEOUT_MILLIS = 1_000;
    private static final int QUEUE_CAPACITY = 10_000;


    private final PacketMapper packetMapper;

    private final TrafficStatisticsService trafficStatisticsService;
    private final DetectionEngine detectionEngine;

    private final GtpUPacketInspector gtpUPacketInspector;
    private final FiveGEnrichmentService fiveGEnrichmentService;
    private final FiveGTrafficStatisticsService fiveGTrafficStatisticsService;
    private final FiveGDetectionEngine fiveGDetectionEngine;

    private final TcpFlowTracker tcpFlowTracker;

    private final BlockingQueue<CapturedTrafficEvent> eventQueue =
            new ArrayBlockingQueue<>(QUEUE_CAPACITY);


    private final ExecutorService captureExecutor =
            Executors.newSingleThreadExecutor(
                    task -> new Thread(task, "javaguard-capture")
            );

    private final ExecutorService processingExecutor =
            Executors.newSingleThreadExecutor(
                    task -> new Thread(task, "javaguard-processor")
            );


    private final AtomicBoolean capturing =
            new AtomicBoolean(false);

    private final AtomicBoolean applicationRunning =
            new AtomicBoolean(true);


    private final AtomicLong capturedPackets =
            new AtomicLong();

    private final AtomicLong processedEvents =
            new AtomicLong();

    private final AtomicLong droppedEvents =
            new AtomicLong();


    private final AtomicReference<NetworkEvent> lastEvent =
            new AtomicReference<>();


    private volatile String interfaceName;

    private volatile PcapHandle handle;


    public CapturePipelineService(
            PacketMapper packetMapper,
            TrafficStatisticsService trafficStatisticsService,
            DetectionEngine detectionEngine,
            GtpUPacketInspector gtpUPacketInspector,
            FiveGEnrichmentService fiveGEnrichmentService,
            FiveGTrafficStatisticsService fiveGTrafficStatisticsService,
            FiveGDetectionEngine fiveGDetectionEngine,
            TcpFlowTracker tcpFlowTracker
    ) {
        this.packetMapper = packetMapper;
        this.trafficStatisticsService = trafficStatisticsService;
        this.detectionEngine = detectionEngine;

        this.gtpUPacketInspector = gtpUPacketInspector;
        this.fiveGEnrichmentService = fiveGEnrichmentService;
        this.fiveGTrafficStatisticsService = fiveGTrafficStatisticsService;
        this.fiveGDetectionEngine = fiveGDetectionEngine;
        this.tcpFlowTracker =tcpFlowTracker;
    }


    @PostConstruct
    public void startProcessor() {
        processingExecutor.submit(this::processingLoop);
    }


    public synchronized CapturePipelineStatus start(String interfaceName) {

        if (capturing.get()) {
            throw new IllegalStateException(
                    "Packet capture is already running"
            );
        }

        try {
            PcapNetworkInterface networkInterface =
                    Pcaps.getDevByName(interfaceName);

            if (networkInterface == null) {
                throw new IllegalArgumentException(
                        "Network interface not found: " + interfaceName
                );
            }

            PcapHandle newHandle =
                    networkInterface.openLive(
                            SNAPSHOT_LENGTH,
                            PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS,
                            READ_TIMEOUT_MILLIS
                    );

            resetStatistics();

            this.interfaceName = interfaceName;
            this.handle = newHandle;

            capturing.set(true);

            captureExecutor.submit(
                    () -> captureLoop(newHandle)
            );

            log.info(
                    "Packet capture started on interface {}",
                    interfaceName
            );

            return getStatus();

        } catch (PcapNativeException exception) {
            throw new IllegalStateException(
                    "Could not start capture on interface: " + interfaceName,
                    exception
            );
        }
    }


    public synchronized CapturePipelineStatus stop() {

        capturing.set(false);

        log.info(
                "Packet capture stop requested for interface {}",
                interfaceName
        );

        return getStatus();
    }


    public CapturePipelineStatus getStatus() {

        return new CapturePipelineStatus(
                capturing.get(),
                interfaceName,
                capturedPackets.get(),
                processedEvents.get(),
                droppedEvents.get(),
                eventQueue.size(),
                QUEUE_CAPACITY,
                lastEvent.get()
        );
    }


    private void captureLoop(PcapHandle captureHandle) {

        try {
            while (capturing.get()) {

                try {
                    Packet packet =
                            captureHandle.getNextPacketEx();

                    capturedPackets.incrementAndGet();

                    packetMapper
                            .map(packet)
                            .ifPresent(
                                    networkEvent ->
                                            enqueue(
                                                    packet,
                                                    networkEvent
                                            )
                            );

                } catch (TimeoutException ignored) {
                    // Нормальная ситуация:
                    // за READ_TIMEOUT_MILLIS пакет не пришёл.
                }
            }

        } catch (
                EOFException
                | PcapNativeException
                | NotOpenException exception
        ) {

            if (capturing.get()) {
                log.error(
                        "Packet capture failed",
                        exception
                );
            }

        } finally {
            capturing.set(false);

            if (captureHandle.isOpen()) {
                captureHandle.close();
            }

            handle = null;

            log.info("Packet capture stopped");
        }
    }


    private void enqueue(
            Packet packet,
            NetworkEvent networkEvent
    ) {

        var gtpUPacket = gtpUPacketInspector
                        .inspect(packet)
                        .orElse(null);

        CapturedTrafficEvent capturedEvent = new CapturedTrafficEvent(
                        networkEvent,
                        gtpUPacket
                );

        boolean added =
                eventQueue.offer(capturedEvent);

        if (!added) {
            droppedEvents.incrementAndGet();

            log.warn(
                    "Capture queue is full. Event dropped: {} -> {}",
                    networkEvent.sourceIp(),
                    networkEvent.destinationIp()
            );
        }
    }


    private void processingLoop() {

        while (applicationRunning.get()) {

            try {
                CapturedTrafficEvent capturedEvent =
                        eventQueue.poll(
                                500,
                                TimeUnit.MILLISECONDS
                        );

                if (capturedEvent == null) {
                    continue;
                }

                try {
                    EnrichedNetworkEvent enrichedEvent =
                            fiveGEnrichmentService.enrich(
                                    capturedEvent
                            );

                    process(enrichedEvent);

                } catch (RuntimeException exception) {
                    log.error(
                            "Failed to process captured event: {}",
                            capturedEvent,
                            exception
                    );
                }

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }


    private void process(
            EnrichedNetworkEvent enrichedEvent
    ) {

        NetworkEvent networkEvent = enrichedEvent.networkEvent();

        tcpFlowTracker.track(networkEvent);


        trafficStatisticsService.record(
                networkEvent
        );

        detectionEngine.analyze(
                networkEvent
        );


        fiveGTrafficStatisticsService.record(
                enrichedEvent
        );

        fiveGDetectionEngine.analyze(
                enrichedEvent
        );


        lastEvent.set(
                networkEvent
        );

        processedEvents.incrementAndGet();


        log.debug(
                "{} {}:{} -> {}:{} size={}",
                networkEvent.protocol(),
                networkEvent.sourceIp(),
                networkEvent.sourcePort(),
                networkEvent.destinationIp(),
                networkEvent.destinationPort(),
                networkEvent.packetSize()
        );


        if (enrichedEvent.isGtpU()) {

            log.debug(
                    "GTP-U packet: TEID={}, contextResolved={}, direction={}",
                    Long.toUnsignedString(
                            enrichedEvent
                                    .gtpUHeader()
                                    .teid()
                    ),
                    enrichedEvent
                            .hasFiveGContext(),
                    enrichedEvent
                            .direction()
            );
        }
    }


    private void resetStatistics() {

        eventQueue.clear();

        capturedPackets.set(0);
        processedEvents.set(0);
        droppedEvents.set(0);

        lastEvent.set(null);

        trafficStatisticsService.reset();
    }


    @PreDestroy
    public void shutdown() {

        capturing.set(false);
        applicationRunning.set(false);

        captureExecutor.shutdownNow();
        processingExecutor.shutdownNow();

        PcapHandle currentHandle = handle;

        if (
                currentHandle != null
                        && currentHandle.isOpen()
        ) {
            currentHandle.close();
        }

        log.info(
                "Capture pipeline shutdown completed"
        );
    }
}