package com.javaguard.ips.fiveg.session;

import com.javaguard.ips.fiveg.model.PduSessionContext;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class FiveGSessionRegistry {

    private final ConcurrentMap<UUID, PduSessionContext> sessions =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, UUID> bySessionKey =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, UUID> byUeIp =
            new ConcurrentHashMap<>();


    private final ConcurrentMap<String, UUID> byTunnel =
            new ConcurrentHashMap<>();


    public synchronized PduSessionContext register(
            PduSessionContext context
    ) {

        String sessionKey =
                createSessionKey(
                        context.supi(),
                        context.pduSessionId()
                );

        UUID currentSessionId =
                bySessionKey.get(sessionKey);

        ensureResourceAvailable(
                byUeIp.get(context.ueIp()),
                currentSessionId,
                "UE IP",
                context.ueIp()
        );

        String uplinkTunnel =
                createTunnelKey(
                        context.upfIp(),
                        context.uplinkTeid()
                );

        String downlinkTunnel =
                createTunnelKey(
                        context.gnbIp(),
                        context.downlinkTeid()
                );

        ensureResourceAvailable(
                byTunnel.get(uplinkTunnel),
                currentSessionId,
                "uplink tunnel",
                uplinkTunnel
        );

        ensureResourceAvailable(
                byTunnel.get(downlinkTunnel),
                currentSessionId,
                "downlink tunnel",
                downlinkTunnel
        );

        if (currentSessionId != null) {
            removeInternal(currentSessionId);
        }

        sessions.put(
                context.id(),
                context
        );

        bySessionKey.put(
                sessionKey,
                context.id()
        );

        byUeIp.put(
                context.ueIp(),
                context.id()
        );

        byTunnel.put(
                uplinkTunnel,
                context.id()
        );

        byTunnel.put(
                downlinkTunnel,
                context.id()
        );

        return context;
    }


    public Optional<PduSessionContext> findById(UUID id) {
        return Optional.ofNullable(
                sessions.get(id)
        );
    }


    public Optional<PduSessionContext> findByUeIp(
            String ueIp
    ) {

        UUID id = byUeIp.get(ueIp);

        if (id == null) {
            return Optional.empty();
        }

        return findById(id);
    }


    public Optional<PduSessionContext> findByTunnel(
            String endpointIp,
            long teid
    ) {

        String key =
                createTunnelKey(
                        endpointIp,
                        teid
                );

        UUID id = byTunnel.get(key);

        if (id == null) {
            return Optional.empty();
        }

        return findById(id);
    }


    public Optional<PduSessionContext> findBySupiAndPduSessionId(
            String supi,
            int pduSessionId
    ) {

        String key =
                createSessionKey(
                        supi,
                        pduSessionId
                );

        UUID id =
                bySessionKey.get(key);

        if (id == null) {
            return Optional.empty();
        }

        return findById(id);
    }


    public List<PduSessionContext> findAll() {

        return sessions
                .values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                PduSessionContext::updatedAt
                        ).reversed()
                )
                .toList();
    }


    public synchronized boolean remove(
            UUID id
    ) {
        return removeInternal(id);
    }


    public int size() {return sessions.size();
    }


    private boolean removeInternal(UUID id) {

        PduSessionContext removed =
                sessions.remove(id);

        if (removed == null) {
            return false;
        }

        bySessionKey.remove(
                createSessionKey(
                        removed.supi(),
                        removed.pduSessionId()
                ),
                id
        );

        byUeIp.remove(
                removed.ueIp(),
                id
        );

        byTunnel.remove(
                createTunnelKey(
                        removed.upfIp(),
                        removed.uplinkTeid()
                ),
                id
        );

        byTunnel.remove(
                createTunnelKey(
                        removed.gnbIp(),
                        removed.downlinkTeid()
                ),
                id
        );

        return true;
    }


    private void ensureResourceAvailable(
            UUID owner,
            UUID currentSessionId,
            String resourceName,
            String value
    ) {

        if (owner != null && !owner.equals(currentSessionId)) {
            throw new IllegalArgumentException(resourceName
                            + " is already assigned to another PDU session: "
                            + value
            );
        }
    }


    private String createSessionKey(
            String supi,
            int pduSessionId
    ) {
        return supi + "#" + pduSessionId;
    }


    private String createTunnelKey(String endpointIp, long teid) {
        return endpointIp
                + "#"
                + Long.toUnsignedString(teid);
    }
}