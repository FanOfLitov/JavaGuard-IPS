package com.javaguard.ips.fiveg.open5gs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Open5GsPduInfoResponse(
        List<UeItem> items
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UeItem(
            String supi,

            @JsonProperty("ue_activity")
            String ueActivity,

            List<PduItem> pdu
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PduItem(
            Integer psi,
            String dnn,
            String ipv4,
            String ipv6,

            Snssai snssai,

            @JsonProperty("pdu_state")
            String pduState
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snssai(
            Integer sst,
            String sd
    ) {
    }
}