package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.open5gs.dto.Open5GsGnbInfoResponse;
import com.javaguard.ips.fiveg.open5gs.dto.Open5GsUeInfoResponse;
import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;
import com.javaguard.ips.fiveg.open5gs.model.GnbContext;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class Open5GsAmfInfoClient {

    private final RestClient restClient;


    public Open5GsAmfInfoClient(
            Open5GsInfoProperties properties
    ) {

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(
                                properties.getAmfBaseUrl()
                        )
                        .build();
    }


    public List<AmfUeContext> fetchUes() {

        Open5GsUeInfoResponse response =
                restClient
                        .get()
                        .uri("/ue-info?page=-1")
                        .retrieve()
                        .body(
                                Open5GsUeInfoResponse.class
                        );

        if (response == null
                || response.items() == null) {

            return List.of();
        }

        Instant observedAt =
                Instant.now();

        List<AmfUeContext> result =
                new ArrayList<>();

        for (Open5GsUeInfoResponse.UeItem item : response.items()) {

            if (item == null
                    || item.supi() == null
                    || item.supi().isBlank()) {

                continue;
            }

            Long gnbId = null;
            Long cellId = null;

            if (item.gnb() != null) {
                gnbId = item.gnb().gnbId();
                cellId = item.gnb().cellId();
            }

            result.add(
                    new AmfUeContext(
                            item.supi()
                                    .trim()
                                    .toLowerCase(
                                            Locale.ROOT
                                    ),
                            item.cmState(),
                            gnbId,
                            cellId,
                            mapUeSlices(
                                    item.requestedSlices()
                            ),
                            mapUeSlices(
                                    item.allowedSlices()
                            ),
                            observedAt
                    )
            );
        }

        return List.copyOf(result);
    }


    public List<GnbContext> fetchGnbs() {

        Open5GsGnbInfoResponse response =
                restClient
                        .get()
                        .uri("/gnb-info?page=-1")
                        .retrieve()
                        .body(
                                Open5GsGnbInfoResponse.class
                        );

        if (response == null
                || response.items() == null) {

            return List.of();
        }

        Instant observedAt =
                Instant.now();

        List<GnbContext> result =
                new ArrayList<>();

        for (Open5GsGnbInfoResponse.GnbItem item : response.items()) {

            if (item == null
                    || item.gnbId() == null) {

                continue;
            }

            String peer = null;
            boolean setupSuccessful = false;

            if (item.ng() != null) {

                setupSuccessful =
                        Boolean.TRUE.equals(
                                item.ng().setupSuccess()
                        );

                if (item.ng().sctp() != null) {
                    peer =
                            item.ng()
                                    .sctp()
                                    .peer();
                }
            }

            int connectedUes =
                    item.numConnectedUes() == null
                            ? 0
                            : item.numConnectedUes();

            result.add(
                    new GnbContext(
                            item.gnbId(),
                            item.plmn(),
                            peer,
                            setupSuccessful,
                            connectedUes,
                            mapGnbSlices(
                                    item.supportedTaList()
                            ),
                            observedAt
                    )
            );
        }

        return List.copyOf(result);
    }


    private List<SliceId> mapUeSlices(
            List<Open5GsUeInfoResponse.Snssai> slices
    ) {

        if (slices == null) {
            return List.of();
        }

        List<SliceId> result =
                new ArrayList<>();

        for (Open5GsUeInfoResponse.Snssai item : slices) {

            if (item == null
                    || item.sst() == null) {

                continue;
            }

            result.add(
                    new SliceId(
                            item.sst(),
                            item.sd()
                    )
            );
        }

        return List.copyOf(result);
    }


    private List<SliceId> mapGnbSlices(
            List<Open5GsGnbInfoResponse.SupportedTa> taList
    ) {

        if (taList == null) {
            return List.of();
        }

        Set<SliceId> result =
                new LinkedHashSet<>();

        for (Open5GsGnbInfoResponse.SupportedTa ta : taList) {

            if (ta == null
                    || ta.bplmns() == null) {

                continue;
            }

            for (Open5GsGnbInfoResponse.Bplmn bplmn : ta.bplmns()) {

                if (bplmn == null
                        || bplmn.snssai() == null) {

                    continue;
                }

                for (Open5GsGnbInfoResponse.Snssai slice : bplmn.snssai()) {

                    if (slice == null
                            || slice.sst() == null) {

                        continue;
                    }

                    result.add(
                            new SliceId(
                                    slice.sst(),
                                    slice.sd()
                            )
                    );
                }
            }
        }

        return List.copyOf(result);
    }
}