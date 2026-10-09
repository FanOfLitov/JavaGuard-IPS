package com.javaguard.ips.statistics;

import com.javaguard.ips.event.NetworkProtocol;
import java.util.Map;

public record TrafficStatisticsSnapshot (
    long totalPackets,
    long totalBytes,
    long packetsPerSecond,
    Map<NetworkProtocol, Long> protocolCounts,
    Map<String, Long> topSourceIpd,
    Map<String, Long> topDestinationIps
){
}
