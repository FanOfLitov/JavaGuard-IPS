package com.javaguard.ips.fiveg.gtpu;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class GtpUParser {

    private static final int MIN_HEADER_LENGTH = 8;
    private static final int OPTIONAL_FIELDS_LENGTH = 4;

    private static final int GTP_VERSION_1 = 1;

    public Optional<GtpUPacketData> parse(byte[] data) {

        if (data == null || data.length < MIN_HEADER_LENGTH) {
            return Optional.empty();
        }

        int flags = Byte.toUnsignedInt(data[0]);

        int version = (flags >> 5) & 0b111;

        boolean protocolType = (flags & 0b0001_0000) != 0;
        boolean extensionHeaderPresent = (flags & 0b0000_0100) != 0;
        boolean sequenceNumberPresent = (flags & 0b0000_0010) != 0;
        boolean nPduNumberPresent = (flags & 0b0000_0001) != 0;

        if (version != GTP_VERSION_1 || !protocolType) {
            return Optional.empty();
        }

        int messageType = Byte.toUnsignedInt(data[1]);

        int messageLength =
                (Byte.toUnsignedInt(data[2]) << 8)
                        | Byte.toUnsignedInt(data[3]);

        int totalLength = MIN_HEADER_LENGTH + messageLength;

        if (totalLength > data.length) {
            return Optional.empty();
        }

        long teid = readUnsignedInt(data, 4);

        int headerLength = MIN_HEADER_LENGTH;

        boolean optionalFieldsPresent =
                extensionHeaderPresent
                        || sequenceNumberPresent
                        || nPduNumberPresent;

        if (optionalFieldsPresent) {

            if (totalLength < MIN_HEADER_LENGTH + OPTIONAL_FIELDS_LENGTH) {
                return Optional.empty();
            }

            headerLength += OPTIONAL_FIELDS_LENGTH;

            int nextExtensionHeaderType =
                    Byte.toUnsignedInt(data[11]);

            if (extensionHeaderPresent) {
                headerLength = parseExtensionHeaders(
                        data,
                        headerLength,
                        totalLength,
                        nextExtensionHeaderType
                );

                if (headerLength < 0) {
                    return Optional.empty();
                }
            }
        }

        if (headerLength > totalLength) {
            return Optional.empty();
        }

        byte[] payload =
                Arrays.copyOfRange(
                        data,
                        headerLength,
                        totalLength
                );

        GtpUHeader header =
                new GtpUHeader(
                        version,
                        protocolType,
                        extensionHeaderPresent,
                        sequenceNumberPresent,
                        nPduNumberPresent,
                        messageType,
                        messageLength,
                        teid,
                        headerLength
                );

        return Optional.of(
                new GtpUPacketData(
                        header,
                        payload
                )
        );
    }

    private int parseExtensionHeaders(
            byte[] data,
            int offset,
            int packetEnd,
            int nextExtensionHeaderType
    ) {

        int currentOffset = offset;
        int currentType = nextExtensionHeaderType;

        while (currentType != 0) {

            if (currentOffset >= packetEnd) {
                return -1;
            }

            int lengthUnits =
                    Byte.toUnsignedInt(
                            data[currentOffset]
                    );

            if (lengthUnits == 0) {
                return -1;
            }

            int extensionLength = lengthUnits * 4;

            int extensionEnd =
                    currentOffset + extensionLength;

            if (extensionEnd > packetEnd) {
                return -1;
            }

            currentType = Byte.toUnsignedInt(
                            data[extensionEnd - 1]
                    );

            currentOffset = extensionEnd;
        }

        return currentOffset;
    }

    private long readUnsignedInt(
            byte[] data,
            int offset
    ) {

        return ((long) Byte.toUnsignedInt(data[offset]) << 24)
                | ((long) Byte.toUnsignedInt(data[offset + 1]) << 16)
                | ((long) Byte.toUnsignedInt(data[offset + 2]) << 8)
                | Byte.toUnsignedInt(data[offset + 3]);
    }
}