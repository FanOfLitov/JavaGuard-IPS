package com.javaguard.ips.capture;

import com.javaguard.ips.event.NetworkEvent;

public record CapturePipelineStatus(
    boolean running,
    String interfaceName,
    long capturedPackets,
    long processedEvents,
    long droppedEvents,
    int queueSize,
    int queueCapacity,
    NetworkEvent lastEvent
){
}
