package com.javaguard.ips.fiveg.open5gs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Open5GsUeInfoResponse(
        List<UeItem> items
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UeItem(
            String supi,

            @JsonProperty("cm_state")
            String cmState,

            GnbRef gnb,

            @JsonProperty("requested_slices")
            List<Snssai> requestedSlices,

            @JsonProperty("allowed_slices")
            List<Snssai> allowedSlices
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GnbRef(
            @JsonProperty("gnb_id")
            Long gnbId,

            @JsonProperty("cell_id")
            Long cellId
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snssai(
            Integer sst,
            String sd
    ) {
    }
}