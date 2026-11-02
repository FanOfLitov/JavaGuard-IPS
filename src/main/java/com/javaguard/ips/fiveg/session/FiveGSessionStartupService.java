package com.javaguard.ips.fiveg.session;

import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.persistence.PduSessionStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class FiveGSessionStartupService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    FiveGSessionStartupService.class
            );

    private final PduSessionStore store;
    private final FiveGSessionRegistry registry;


    public FiveGSessionStartupService(
            PduSessionStore store,
            FiveGSessionRegistry registry
    ) {
        this.store = store;
        this.registry = registry;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void restoreSessions() {

        int restored = 0;

        for (PduSessionContext session : store.findAll()) {

            try {
                registry.register(session);
                restored++;

            } catch (RuntimeException exception) {
                log.error(
                        "Failed to restore PDU session {}",
                        session.id(),
                        exception
                );
            }
        }

        log.info(
                "Restored {} persisted 5G PDU sessions",
                restored
        );
    }
}