package com.javaguard.ips.response;

import com.javaguard.ips.response.config.ResponseProperties;
import com.javaguard.ips.web.dto.ResponseStatusResponse;

import org.springframework.stereotype.Service;


@Service
public class ResponseStatusService {

    private final ResponseProperties
            properties;

    private final ThreatResponseService
            threatResponseService;

    private final ProtectedAddressService
            protectedAddressService;

    private final AllowlistService
            allowlistService;


    public ResponseStatusService(
            ResponseProperties properties,
            ThreatResponseService threatResponseService,
            ProtectedAddressService protectedAddressService,
            AllowlistService allowlistService
    ) {

        this.properties =
                properties;

        this.threatResponseService =
                threatResponseService;

        this.protectedAddressService =
                protectedAddressService;

        this.allowlistService =
                allowlistService;
    }


    public ResponseStatusResponse getStatus() {

        return new ResponseStatusResponse(

                properties.getMode(),

                properties.isAutoBlockEnabled(),

                threatResponseService
                        .getBlockedCount(),

                allowlistService
                        .count(),

                protectedAddressService
                        .getProtectedAddresses()
                        .size()
        );
    }
}