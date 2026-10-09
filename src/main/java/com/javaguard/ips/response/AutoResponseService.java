package com.javaguard.ips.response;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.response.model.ResponseDecision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class AutoResponseService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AutoResponseService.class
            );


    private final ResponsePolicyService
            policyService;

    private final ThreatResponseService
            responseService;


    public AutoResponseService(
            ResponsePolicyService policyService,
            ThreatResponseService responseService
    ) {

        this.policyService =
                policyService;

        this.responseService =
                responseService;
    }


    public void handle(
            SecurityEvent event
    ) {

        ResponseDecision decision =
                policyService.evaluate(event);


        if (!decision.shouldBlock()) {

            log.debug(
                    "No automatic response for event {}",
                    event.id()
            );

            return;
        }


        responseService.block(
                event.sourceIp(),
                decision.duration(),
                decision.reason()
        );


        log.warn(
                "Automatic response applied: sourceIp={}, type={}, duration={}",
                event.sourceIp(),
                event.type(),
                decision.duration()
        );
    }
}