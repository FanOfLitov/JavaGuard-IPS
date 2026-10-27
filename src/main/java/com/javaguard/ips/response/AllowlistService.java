package com.javaguard.ips.response;

import com.javaguard.ips.persistence.AllowlistStore;

import com.javaguard.ips.response.model.AllowlistEntry;

import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;


@Service
public class AllowlistService {

    private final AllowlistStore store;


    public AllowlistService(AllowlistStore store) {

        this.store = store;
    }


    public AllowlistEntry add(String ip, String description
    ) {

        validateIp(ip);


        String normalizedIp = ip.trim();


        if (
                store.contains(normalizedIp)
        ) {

            throw new IllegalArgumentException(
                    "IP address is already allowlisted: "
                            + normalizedIp
            );
        }


        AllowlistEntry entry =
                new AllowlistEntry(
                        normalizedIp,
                        normalizeDescription(
                                description
                        ),
                        Instant.now()
                );


        return store.save(entry);
    }


    public boolean remove(String ip
    ) {

        validateIp(ip);


        return store.delete(ip.trim()
        );
    }


    public boolean isAllowed(String ip) {

        if (
                ip == null || ip.isBlank()
        ) {

            return false;
        }


        return store.contains(ip.trim());
    }


    public List<AllowlistEntry> getAll() {

        return store.findAll();
    }


    private void validateIp(String ip) {

        if (
                ip == null || ip.isBlank()
        ) {

            throw new IllegalArgumentException("IP address must not be empty");
        }
    }


    private String normalizeDescription(
            String description
    ) {

        if (
                description == null
                        ||
                        description.isBlank()
        ) {

            return "Trusted address";
        }


        return description.trim();
    }
}