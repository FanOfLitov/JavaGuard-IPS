package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ControlPlaneSessionRegistry {

    private final ConcurrentMap<String, ControlPlanePduSession> bySessionKey = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, String> sessionKeyByUeIp = new ConcurrentHashMap<>();


    public synchronized void replaceAll(
            Collection<ControlPlanePduSession> sessions
    ) {

        bySessionKey.clear();
        sessionKeyByUeIp.clear();

        for (ControlPlanePduSession session : sessions) {
            String key = session.sessionKey();

            bySessionKey.put(key, session);

            sessionKeyByUeIp.put(session.ueIp(), key);
        }
    }


    public Optional<ControlPlanePduSession>
    findBySupiAndPduSessionId(String supi, int pduSessionId) {

        return Optional.ofNullable(bySessionKey.get(supi + "#" + pduSessionId)
        );
    }


    public Optional<ControlPlanePduSession> findByUeIp(
            String ueIp
    ) {

        String key =
                sessionKeyByUeIp.get(ueIp);

        if (key == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                bySessionKey.get(key)
        );
    }


    public List<ControlPlanePduSession> findAll() {

        List<ControlPlanePduSession> result =
                new ArrayList<>(
                        bySessionKey.values()
                );

        result.sort(
                Comparator
                        .comparing(
                                ControlPlanePduSession::supi
                        )
                        .thenComparingInt(
                                ControlPlanePduSession::pduSessionId
                        )
        );

        return List.copyOf(result);
    }


    public int size() {
        return bySessionKey.size();
    }
}