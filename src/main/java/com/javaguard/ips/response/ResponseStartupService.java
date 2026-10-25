package com.javaguard.ips.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import org.springframework.stereotype.Service;


@Service
public class ResponseStartupService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ResponseStartupService.class
            );


    private final ThreatResponseService
            responseService;


    public ResponseStartupService(
            ThreatResponseService responseService
    ) {

        this.responseService =
                responseService;
    }


    @EventListener(
            ApplicationReadyEvent.class
    )
    public void restoreResponseState() {

        log.info(
                "Restoring JavaGuard response state"
        );


        responseService
                .restoreActiveBlocks();


        log.info(
                "JavaGuard response state restored"
        );
    }
}