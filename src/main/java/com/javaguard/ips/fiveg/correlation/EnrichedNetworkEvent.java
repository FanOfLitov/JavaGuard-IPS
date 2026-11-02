package com.javaguard.ips.fiveg.correlation;

import com.javaguard.ips.event.NetworkEvent;

import com.javaguard.ips.fiveg.gtpu.GtpUHeader;
import com.javaguard.ips.fiveg.gtpu.InnerIpPacketInfo;

import com.javaguard.ips.fiveg.model.PduSessionContext;

public record EnrichedNetworkEvent(
        NetworkEvent networkEvent,
        GtpUHeader gtpUHeader,
        InnerIpPacketInfo innerPacket,
        PduSessionContext pduSession,
        FiveGDirection direction
) {

    public boolean isGtpU() {
        return gtpUHeader != null;
    }

    public boolean hasFiveGContext() {
        return pduSession != null;
    }

    public boolean hasInnerPacket() {
        return innerPacket != null;
    }

    public boolean hasKnownTeid() {
        return isGtpU() && gtpUHeader.hasTeid() && hasFiveGContext();
    }
}