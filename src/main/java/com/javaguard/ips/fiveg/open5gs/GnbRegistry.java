package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.open5gs.model.GnbContext;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class GnbRegistry {

    private final ConcurrentMap<Long, GnbContext> byId =
            new ConcurrentHashMap<>();


    public synchronized void replaceAll(
            Collection<GnbContext> contexts
    ) {

        byId.clear();

        for (GnbContext context : contexts) {
            byId.put(
                    context.gnbId(),
                    context
            );
        }
    }


    public Optional<GnbContext> findById(
            long gnbId
    ) {
        return Optional.ofNullable(
                byId.get(gnbId)
        );
    }


    public List<GnbContext> findAll() {

        return byId
                .values()
                .stream()
                .sorted(
                        Comparator.comparingLong(
                                GnbContext::gnbId
                        )
                )
                .toList();
    }


    public int size() {
        return byId.size();
    }
}