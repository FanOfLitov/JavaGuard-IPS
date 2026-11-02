package com.javaguard.ips.web.dto;


public record RegisterPduSessionRequest(

        String supi,

        int pduSessionId,

        String ueIp,

        String dnn,

        int sst,

        String sd,

        String gnbIp,

        String upfIp,

        long uplinkTeid,

        long downlinkTeid

) {
}