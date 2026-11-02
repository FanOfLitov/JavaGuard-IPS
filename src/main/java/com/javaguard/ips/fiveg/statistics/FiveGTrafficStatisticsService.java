package com.javaguard.ips.fiveg.statistics;

import com.javaguard.ips.fiveg.correlation.EnrichedNetworkEvent;
import com.javaguard.ips.fiveg.model.PduSessionContext;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


@Service
public class FiveGTrafficStatisticsService {

    private final LongAdder gtpUPackets =
            new LongAdder();


    private final LongAdder gtpUBytes =
            new LongAdder();


    private final LongAdder tPduPackets =
            new LongAdder();


    private final LongAdder resolvedContextPackets =
            new LongAdder();


    private final LongAdder unknownTeidPackets =
            new LongAdder();


    private final Map<String, LongAdder>
            packetsByDnn =
            new ConcurrentHashMap<>();


    private final Map<String, LongAdder>
            packetsBySlice =
            new ConcurrentHashMap<>();


    public void record(
            EnrichedNetworkEvent event
    ) {

        if (!event.isGtpU()) {

            return;
        }


        gtpUPackets.increment();


        gtpUBytes.add(
                event.networkEvent()
                        .packetSize()
        );


        if (
                event.gtpUHeader()
                        .isTPdu()
        ) {

            tPduPackets.increment();
        }


        PduSessionContext context =
                event.pduSession();


        if (context == null) {

            if (
                    event.gtpUHeader()
                            .hasTeid()
            ) {

                unknownTeidPackets.increment();
            }


            return;
        }


        resolvedContextPackets.increment();


        packetsByDnn
                .computeIfAbsent(
                        context.dnn(),
                        ignored ->
                                new LongAdder()
                )
                .increment();


        String slice =
                context.slice()
                        .displayName();


        packetsBySlice
                .computeIfAbsent(
                        slice,
                        ignored ->
                                new LongAdder()
                )
                .increment();
    }


    public FiveGTrafficStatisticsSnapshot snapshot() {

        return new FiveGTrafficStatisticsSnapshot(

                gtpUPackets.sum(),

                gtpUBytes.sum(),

                tPduPackets.sum(),

                resolvedContextPackets.sum(),

                unknownTeidPackets.sum(),

                snapshotMap(
                        packetsByDnn
                ),

                snapshotMap(
                        packetsBySlice
                )
        );
    }


    private Map<String, Long> snapshotMap(
            Map<String, LongAdder> source
    ) {

        Map<String, Long> result =
                new LinkedHashMap<>();


        source
                .entrySet()
                .stream()
                .sorted(
                        (left, right) ->
                                Long.compare(
                                        right.getValue().sum(),
                                        left.getValue().sum()
                                )
                )
                .forEach(
                        entry ->
                                result.put(
                                        entry.getKey(),
                                        entry.getValue().sum()
                                )
                );


        return result;
    }
}