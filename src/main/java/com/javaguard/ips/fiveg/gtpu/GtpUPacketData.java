package com.javaguard.ips.fiveg.gtpu;

public record GtpUPacketData(
        GtpUHeader header,
        byte[] payload
) {

    public boolean hasPayload() {
        return payload != null && payload.length > 0;
    }
}