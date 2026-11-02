package com.javaguard.ips.fiveg.correlation;

import com.javaguard.ips.event.NetworkEvent;
import com.javaguard.ips.fiveg.gtpu.GtpUHeader;
import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.session.FiveGSessionRegistry;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FiveGContextResolver {

    private final FiveGSessionRegistry sessionRegistry;

    public FiveGContextResolver(
            FiveGSessionRegistry sessionRegistry
    ) {
        this.sessionRegistry = sessionRegistry;
    }


    public Optional<PduSessionContext> resolve(
            NetworkEvent event,
            GtpUHeader gtpUHeader
    ) {

        if (event == null) {
            return Optional.empty();
        }

        if (gtpUHeader != null && gtpUHeader.hasTeid()) {

            Optional<PduSessionContext> byTunnel =
                    sessionRegistry.findByTunnel(
                            event.destinationIp(),
                            gtpUHeader.teid()
                    );

            if (byTunnel.isPresent()) {
                return byTunnel;
            }
        }

        Optional<PduSessionContext> bySource =
                sessionRegistry.findByUeIp(
                        event.sourceIp()
                );

        if (bySource.isPresent()) {
            return bySource;
        }

        return sessionRegistry.findByUeIp(
                event.destinationIp()
        );
    }
}