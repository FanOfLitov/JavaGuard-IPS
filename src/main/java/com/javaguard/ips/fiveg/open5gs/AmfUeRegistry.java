package com.javaguard.ips.fiveg.open5gs;

import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AmfUeRegistry {

    private final ConcurrentMap<String, AmfUeContext> bySupi =
            new ConcurrentHashMap<>();


    public synchronized void replaceAll(
            Collection<AmfUeContext> contexts
    ) {

        bySupi.clear();

        for (AmfUeContext context : contexts) {
            bySupi.put(
                    context.supi(),
                    context
            );
        }
    }


    public Optional<AmfUeContext> findBySupi(
            String supi
    ) {

        if (supi == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                bySupi.get(
                        supi.toLowerCase()
                )
        );
    }


    public List<AmfUeContext> findAll() {

        return bySupi
                .values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                AmfUeContext::supi
                        )
                )
                .toList();
    }


    public int size() {
        return bySupi.size();
    }
}