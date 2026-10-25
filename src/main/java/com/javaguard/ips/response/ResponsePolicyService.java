package com.javaguard.ips.response;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.ThreatType;

import com.javaguard.ips.response.config.ResponseProperties;
import com.javaguard.ips.response.model.ResponseDecision;

import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class ResponsePolicyService {

    private final ProtectedAddressService
            protectedAddressService;

    private final ResponseProperties
            properties;


    public ResponsePolicyService(
            ProtectedAddressService protectedAddressService,
            ResponseProperties properties
    ) {

        this.protectedAddressService =
                protectedAddressService;

        this.properties =
                properties;
    }


    public ResponseDecision evaluate(
            SecurityEvent event
    ) {

        if (event == null) {

            return ResponseDecision.ignore();
        }


        if (
                !properties
                        .isAutoBlockEnabled()
        ) {

            return ResponseDecision.ignore();
        }


        if (
                protectedAddressService
                        .isProtected(
                                event.sourceIp()
                        )
        ) {

            return ResponseDecision.ignore();
        }


        if (
                event.type()
                        == ThreatType.PORT_SCAN
        ) {

            return ResponseDecision.block(
                    Duration.ofMinutes(5),
                    "Automatic response to PORT_SCAN"
            );
        }


        if (
                event.type()
                        == ThreatType.SYN_FLOOD
        ) {

            return ResponseDecision.block(
                    Duration.ofMinutes(15),
                    "Automatic response to SYN_FLOOD"
            );
        }


        return ResponseDecision.ignore();
    }
}