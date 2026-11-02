package com.javaguard.ips.fiveg.gtpu;

public record GtpUHeader(
        int version,
        boolean protocolType,
        boolean extensionHeaderPresent,
        boolean sequenceNumberPresent,
        boolean nPduNumberPresent,
        int messageType,
        int messageLength,
        long teid,
        int headerLength
) {

    public static final int T_PDU_MESSAGE_TYPE = 0xFF;

    public boolean isTPdu() {
        return messageType == T_PDU_MESSAGE_TYPE;
    }

    public boolean hasTeid() {
        return teid != 0;
    }
}