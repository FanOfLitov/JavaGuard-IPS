package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.fiveg.open5gs.dto.Open5GsPduInfoResponse;
import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;
import com.javaguard.ips.response.validation.IpAddressParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class Open5GsInfoClient {

    private static final Logger log =
            LoggerFactory.getLogger(
                    Open5GsInfoClient.class
            );

    private final RestClient restClient;
    private final IpAddressParser ipAddressParser;


    public Open5GsInfoClient(
            Open5GsInfoProperties properties,
            IpAddressParser ipAddressParser
    ) {

        this.restClient = RestClient
                        .builder()
                        .baseUrl(
                                properties.getSmfBaseUrl()
                        )
                        .build();

        this.ipAddressParser =
                ipAddressParser;
    }


    public List<ControlPlanePduSession> fetchPduSessions() {

        Open5GsPduInfoResponse response =
                restClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/pdu-info")
                                                .queryParam(
                                                        "page",
                                                        -1
                                                )
                                                .build()
                        )
                        .retrieve()
                        .body(
                                Open5GsPduInfoResponse.class
                        );

        if (response == null
                || response.items() == null) {

            return List.of();
        }

        List<ControlPlanePduSession> result =
                new ArrayList<>();

        Instant observedAt =
                Instant.now();

        for (
                Open5GsPduInfoResponse.UeItem ue :
                response.items()
        ) {

            if (ue == null
                    || ue.supi() == null
                    || ue.pdu() == null) {

                continue;
            }

            for (
                    Open5GsPduInfoResponse.PduItem pdu :
                    ue.pdu()
            ) {

                mapSession(
                        ue,
                        pdu,
                        observedAt
                )
                        .ifPresent(
                                result::add
                        );
            }
        }

        return List.copyOf(result);
    }


    private java.util.Optional<ControlPlanePduSession>
    mapSession(
            Open5GsPduInfoResponse.UeItem ue,
            Open5GsPduInfoResponse.PduItem pdu,
            Instant observedAt
    ) {

        if (pdu == null
                || pdu.psi() == null
                || pdu.dnn() == null
                || pdu.snssai() == null
                || pdu.snssai().sst() == null) {

            return java.util.Optional.empty();
        }

        String ueIp =
                chooseUeIp(
                        pdu.ipv4(),
                        pdu.ipv6()
                );

        if (ueIp == null) {
            return java.util.Optional.empty();
        }

        try {
            String normalizedIp =
                    ipAddressParser
                            .parse(ueIp)
                            .value();

            SliceId slice =
                    new SliceId(
                            pdu.snssai().sst(),
                            pdu.snssai().sd()
                    );

            ControlPlanePduSession session =
                    new ControlPlanePduSession(
                            ue.supi()
                                    .trim()
                                    .toLowerCase(
                                            Locale.ROOT
                                    ),
                            pdu.psi(),
                            normalizedIp,
                            pdu.dnn()
                                    .trim()
                                    .toLowerCase(
                                            Locale.ROOT
                                    ),
                            slice,
                            pdu.pduState(),
                            ue.ueActivity(),
                            observedAt
                    );

            return java.util.Optional.of(
                    session
            );

        } catch (RuntimeException exception) {

            log.warn(
                    "Ignoring invalid Open5GS PDU session: SUPI={}, PSI={}",
                    ue.supi(),
                    pdu.psi(),
                    exception
            );

            return java.util.Optional.empty();
        }
    }


    private String chooseUeIp(
            String ipv4,
            String ipv6
    ) {

        if (ipv4 != null
                && !ipv4.isBlank()) {

            return ipv4.trim();
        }

        if (ipv6 != null
                && !ipv6.isBlank()) {

            return ipv6.trim();
        }

        return null;
    }
}