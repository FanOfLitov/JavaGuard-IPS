package com.javaguard.ips.capture.model;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.fiveg.gtpu.GtpUPacketData;

public record CapturedTrafficEvent(
        NetworkEvent networkEvent,
        GtpUPacketData gtpU
) {

    public boolean isGtpU() {
        return gtpU != null;
    }
}