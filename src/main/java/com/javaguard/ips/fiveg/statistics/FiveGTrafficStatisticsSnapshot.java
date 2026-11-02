package com.javaguard.ips.fiveg.statistics;

import java.util.Map;


public record FiveGTrafficStatisticsSnapshot(

        long gtpUPackets,

        long gtpUBytes,

        long tPduPackets,

        long resolvedContextPackets,

        long unknownTeidPackets,

        Map<String, Long> packetsByDnn,

        Map<String, Long> packetsBySlice

) {
}