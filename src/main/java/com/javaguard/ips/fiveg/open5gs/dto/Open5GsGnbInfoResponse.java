package com.javaguard.ips.fiveg.open5gs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Open5GsGnbInfoResponse(List<GnbItem> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GnbItem(
            @JsonProperty("gnb_id")
            Long gnbId,

            String plmn,

            NgInfo ng,

            @JsonProperty("supported_ta_list")
            List<SupportedTa> supportedTaList,

            @JsonProperty("num_connected_ues")
            Integer numConnectedUes
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NgInfo(
            SctpInfo sctp,

            @JsonProperty("setup_success")
            Boolean setupSuccess
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SctpInfo(
            String peer
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SupportedTa(
            String tac,
            List<Bplmn> bplmns
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bplmn(
            String plmn,
            List<Snssai> snssai
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snssai(
            Integer sst,
            String sd
    ) {
    }
}