package com.javaguard.ips.fiveg.session;

import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.model.SliceId;
import com.javaguard.ips.persistence.PduSessionStore;
import com.javaguard.ips.response.validation.IpAddressParser;
import com.javaguard.ips.web.dto.RegisterPduSessionRequest;

import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class FiveGSessionService {

    private static final long MAX_TEID =
            0xFFFF_FFFFL;

    private final FiveGSessionRegistry registry;
    private final PduSessionStore store;
    private final IpAddressParser ipAddressParser;


    public FiveGSessionService(
            FiveGSessionRegistry registry,
            PduSessionStore store,
            IpAddressParser ipAddressParser
    ) {
        this.registry = registry;
        this.store = store;
        this.ipAddressParser = ipAddressParser;
    }


    public synchronized PduSessionContext register(
            RegisterPduSessionRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "PDU session request must not be null"
            );
        }

        String supi =
                requireText(
                        request.supi(),
                        "SUPI"
                )
                        .toLowerCase(Locale.ROOT);

        String dnn =
                requireText(
                        request.dnn(),
                        "DNN"
                )
                        .toLowerCase(Locale.ROOT);

        validatePduSessionId(
                request.pduSessionId()
        );

        validateTeid(
                request.uplinkTeid(),
                "uplink TEID"
        );

        validateTeid(
                request.downlinkTeid(),
                "downlink TEID"
        );

        String ueIp =
                ipAddressParser
                        .parse(request.ueIp())
                        .value();

        String gnbIp =
                ipAddressParser
                        .parse(request.gnbIp())
                        .value();

        String upfIp =
                ipAddressParser
                        .parse(request.upfIp())
                        .value();

        SliceId slice =
                new SliceId(
                        request.sst(),
                        request.sd()
                );

        Optional<PduSessionContext> previous =
                registry.findBySupiAndPduSessionId(
                        supi,
                        request.pduSessionId()
                );

        PduSessionContext context =
                new PduSessionContext(
                        UUID.randomUUID(),
                        supi,
                        request.pduSessionId(),
                        ueIp,
                        dnn,
                        slice,
                        gnbIp,
                        upfIp,
                        request.uplinkTeid(),
                        request.downlinkTeid(),
                        Instant.now()
                );

        registry.register(context);

        try {
            return store.save(context);

        } catch (RuntimeException exception) {

            registry.remove(context.id());

            previous.ifPresent(
                    registry::register
            );

            throw exception;
        }
    }


    public List<PduSessionContext> findAll() {
        return registry.findAll();
    }


    public Optional<PduSessionContext> findByUeIp(
            String ueIp
    ) {

        String normalized =
                ipAddressParser
                        .parse(ueIp)
                        .value();

        return registry.findByUeIp(
                normalized
        );
    }


    public Optional<PduSessionContext> findByTunnel(
            String endpointIp,
            long teid
    ) {

        validateTeid(
                teid,
                "TEID"
        );

        String normalizedEndpoint =
                ipAddressParser
                        .parse(endpointIp)
                        .value();

        return registry.findByTunnel(
                normalizedEndpoint,
                teid
        );
    }


    public synchronized boolean remove(
            UUID id
    ) {

        boolean removedFromDatabase =
                store.delete(id);

        boolean removedFromRegistry =
                registry.remove(id);

        return removedFromDatabase
                || removedFromRegistry;
    }


    public int count() {
        return registry.size();
    }


    private void validatePduSessionId(
            int value
    ) {

        if (value < 1 || value > 255) {
            throw new IllegalArgumentException(
                    "PDU Session ID must be between 1 and 255"
            );
        }
    }


    private void validateTeid(
            long value,
            String field
    ) {

        if (value <= 0 || value > MAX_TEID) {
            throw new IllegalArgumentException(
                    field
                            + " must be between 1 and "
                            + MAX_TEID
            );
        }
    }


    private String requireText(
            String value,
            String field
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be empty"
            );
        }

        return value.trim();
    }
}