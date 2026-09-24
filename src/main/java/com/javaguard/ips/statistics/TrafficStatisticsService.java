package com.javaguard.ips.statistics;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.event.NetworkProtocol;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;


public class TrafficStatisticsService {

    private static final int TOP_IP_LIMIT =10;
    private final LongAdder totalPackets = new LongAdder();

    private final LongAdder totalBytes = new LongAdder();

    private final Map<NetworkProtocol, LongAdder> protocolCounts = new ConcurrentHashMap<>();

    private final Map<String, LongAdder> destinationIpCounts = new ConcurrentHashMap<>();

    private final AtomicLong currentSecond = new AtomicLong(Instant.now().getEpochSecond());
}
