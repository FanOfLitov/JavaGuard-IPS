package com.javaguard.ips.response;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.ThreatType;

import com.javaguard.ips.response.config.ResponseProperties;
import com.javaguard.ips.response.model.ResponseDecision;

import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class ResponsePolicyService {

    private final ProtectedAddressService protectedAddressService;

    private final ResponseProperties properties;
    private final AllowlistService allowlistService;


    public ResponsePolicyService(
            ProtectedAddressService protectedAddressService,
            ResponseProperties properties,
            AllowlistService allowlistService
    ) {

        this.protectedAddressService = protectedAddressService;

        this.properties = properties;

        this.allowlistService = allowlistService;
    }


    public ResponseDecision evaluate(SecurityEvent event) {

        if (event == null) {
            return ResponseDecision.ignore();
        }

        if (!properties.isAutoBlockEnabled()) {
            return ResponseDecision.ignore();
        }

        if (protectedAddressService.isProtected(event.sourceIp())) {
            return ResponseDecision.ignore();
        }

        if (allowlistService.isAllowed(event.sourceIp())) {
            return ResponseDecision.ignore();
        }

        return switch (event.type()) {

            case PORT_SCAN ->
                    ResponseDecision.block(
                            Duration.ofMinutes(5),
                            "Automatic response to PORT_SCAN"
                    );

            case HORIZONTAL_SCAN ->
                    ResponseDecision.block(
                            Duration.ofMinutes(10),
                            "Automatic response to HORIZONTAL_SCAN"
                    );

            case SYN_FLOOD ->
                    ResponseDecision.block(
                            Duration.ofMinutes(15),
                            "Automatic response to SYN_FLOOD"
                    );

            case UDP_FLOOD ->
                    ResponseDecision.block(
                            Duration.ofMinutes(10),
                            "Automatic response to UDP_FLOOD"
                    );

            case ICMP_FLOOD ->
                    ResponseDecision.block(
                            Duration.ofMinutes(5),
                            "Automatic response to ICMP_FLOOD"
                    );


            case UNKNOWN_GTP_TEID,
                 UE_IP_MISMATCH,
                 SLICE_POLICY_VIOLATION,
                 STALE_PDU_SESSION,
                 CONTROL_PLANE_CONTEXT_MISMATCH ->
                    ResponseDecision.ignore();

            case HALF_OPEN_CONNECTION_FLOOD ->
                    ResponseDecision.block(
                            Duration.ofMinutes(20),
                            "Automatic response to HALF_OPEN_CONNECTION_FLOOD"
                    );

            case TCP_RESET_BURST ->
                    ResponseDecision.block(
                            Duration.ofMinutes(10),
                            "Automatic response to TCP_RESET_BURST"
                    );

            case TCP_CONNECTION_FAILURE_ANOMALY ->
                    ResponseDecision.ignore();
        };
    }
}