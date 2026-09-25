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

@Service
public class TrafficStatisticsService {

    private static final int TOP_IP_LIMIT =10;
    private final LongAdder totalPackets = new LongAdder();

    private final LongAdder totalBytes = new LongAdder();

    private final Map<NetworkProtocol, LongAdder> protocolCounts = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> sourceIpCounts = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> destinationIpCounts = new ConcurrentHashMap<>();

    private final AtomicLong currentSecond = new AtomicLong(Instant.now().getEpochSecond());

    private final AtomicLong packetsInCurrentSecond = new AtomicLong();

    private final AtomicLong lastPacketsPerSecond = new AtomicLong();

    public void record(NetworkEvent event){
        totalPackets.increment();

        totalBytes.add(event.packetSize());

        protocolCounts.computeIfAbsent(event.protocol(), protocol -> new LongAdder()).increment();

        sourceIpCounts.computeIfAbsent(event.sourceIp(), ip-> new LongAdder()).increment();

        destinationIpCounts.computeIfAbsent(event.destinationIp(), ip -> new LongAdder()).increment();
        updatePacketsPerSecond();



    }


    public TrafficStatisticsSnapshot snapshot(){

        return new TrafficStatisticsSnapshot(
                totalPackets.sum(),
                totalBytes.sum(),
                currentPacketsPerSecond(),
                buildProtocolCounts(),
                topIps(sourceIpCounts),
                topIps(destinationIpCounts)

        );
    }
    
    private void updatePacketsPerSecond(){
        
        long now = Instant.now().getEpochSecond();
        long second = currentSecond.get();
        
        if(now !=second){
            if(currentSecond.compareAndSet(
                second,
                now
            )){
                lastPacketsPerSecond.set(
                        packetsInCurrentSecond.getAndSet(0));
                
            }
        }

    
    packetsInCurrentSecond.incrementAndGet();
    
    
}
    public void reset(){
        totalPackets.reset();
        totalBytes.reset();

        protocolCounts.clear();
        sourceIpCounts.clear();
        destinationIpCounts.clear();

        currentSecond.set(Instant.now().getEpochSecond());

        packetsInCurrentSecond.set(0);
        lastPacketsPerSecond.set(0);
    }

    private long currentPacketsPerSecond(){
        long now = Instant.now().getEpochSecond();

        if(now == currentSecond.get()) {
            return packetsInCurrentSecond.get();
        }

        return lastPacketsPerSecond.get();

    }

    private Map<NetworkProtocol, Long> buildProtocolCounts(){
        Map<NetworkProtocol, Long> result = new EnumMap<>(NetworkProtocol.class);

        for(
                NetworkProtocol protocol:NetworkProtocol.values()){
            LongAdder counter = protocolCounts.get(protocol);

            long count= counter == null
                    ? 0
                    : counter.sum();

            result.put(
                    protocol,
                    count
            );
        }
        return Map.copyOf(result);

    }

    private Map<String, Long> topIps(
            Map<String, LongAdder> counters
    ){

    return counters
            .entrySet()
            .stream()
            .sorted(
                    (first, second) ->
                            Long.compare(
                                    second
                                            .getValue()
                                            .sum(),
                                    first
                                            .getValue()
                                            .sum()
                            )
            )
            .limit(TOP_IP_LIMIT)
            .collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            entry ->
                                    entry
                                            .getValue()
                                            .sum(),
                            (first, second) -> first,
                            LinkedHashMap::new
                    )
            );
    }
}



