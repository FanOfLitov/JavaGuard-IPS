package com.javaguard.ips.fiveg.model;

import java.time.Instant;
import java.util.UUID;


public record PduSessionContext(

        UUID id,

        String supi,

        int pduSessionId,

        String ueIp,

        String dnn,

        SliceId slice,

        String gnbIp,

        String upfIp,

        long uplinkTeid,

        long downlinkTeid,

        Instant updatedAt

) {
}