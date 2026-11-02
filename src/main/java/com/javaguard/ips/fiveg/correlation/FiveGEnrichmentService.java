package com.javaguard.ips.fiveg.correlation;

import com.javaguard.ips.capture.model.CapturedTrafficEvent;

import com.javaguard.ips.fiveg.gtpu.GtpUHeader;
import com.javaguard.ips.fiveg.gtpu.InnerIpPacketInfo;
import com.javaguard.ips.fiveg.gtpu.InnerIpPacketParser;

import com.javaguard.ips.fiveg.model.PduSessionContext;

import org.springframework.stereotype.Service;

@Service
public class FiveGEnrichmentService {

    private final FiveGContextResolver contextResolver;
    private final InnerIpPacketParser innerIpPacketParser;

    public FiveGEnrichmentService(
            FiveGContextResolver contextResolver,
            InnerIpPacketParser innerIpPacketParser
    ) {
        this.contextResolver = contextResolver;
        this.innerIpPacketParser = innerIpPacketParser;
    }

    public EnrichedNetworkEvent enrich(
            CapturedTrafficEvent captured
    ) {

        GtpUHeader header = null;
        InnerIpPacketInfo innerPacket = null;

        if (captured.gtpU() != null) {

            header = captured
                            .gtpU()
                            .header();

            if (header.isTPdu() && captured.gtpU().hasPayload()) {

                innerPacket = innerIpPacketParser
                                .parse(
                                        captured
                                                .gtpU()
                                                .payload()
                                )
                                .orElse(null);
            }
        }

        PduSessionContext context = contextResolver
                        .resolve(
                                captured.networkEvent(),
                                header
                        )
                        .orElse(null);

        FiveGDirection direction =
                resolveDirection(
                        header,
                        context
                );

        return new EnrichedNetworkEvent(
                captured.networkEvent(),
                header,
                innerPacket,
                context,
                direction
        );
    }

    private FiveGDirection resolveDirection(GtpUHeader header, PduSessionContext context) {

        if (header == null) {
            return FiveGDirection.NONE;
        }

        if (context == null) {
            return FiveGDirection.UNKNOWN;
        }

        if (header.teid() == context.uplinkTeid()) {
            return FiveGDirection.UPLINK;
        }

        if (header.teid() == context.downlinkTeid()) {
            return FiveGDirection.DOWNLINK;
        }

        return FiveGDirection.UNKNOWN;
    }
}