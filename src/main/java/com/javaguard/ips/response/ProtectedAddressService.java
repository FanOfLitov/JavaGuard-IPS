package com.javaguard.ips.response;

import com.javaguard.ips.response.config.ResponseProperties;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Service
public class ProtectedAddressService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProtectedAddressService.class
            );


    private final ResponseProperties properties;


    private final Set<String> protectedAddresses =
            new HashSet<>();


    public ProtectedAddressService(
            ResponseProperties properties
    ) {

        this.properties = properties;
    }


    @PostConstruct
    public void initialize() {

        protectedAddresses.clear();


        properties
                .getProtectedAddresses()
                .stream()
                .map(String::trim)
                .filter(
                        value ->
                                !value.isBlank()
                )
                .forEach(
                        protectedAddresses::add
                );


        loadLocalAddresses();


        log.info(
                "Protected response addresses: {}",
                protectedAddresses
        );
    }


    public boolean isProtected(
            String ip
    ) {

        if (
                ip == null
                        ||
                        ip.isBlank()
        ) {

            return true;
        }


        String normalized =
                normalize(ip);


        return protectedAddresses
                .contains(normalized);
    }


    public Set<String> getProtectedAddresses() {

        return Set.copyOf(
                protectedAddresses
        );
    }


    private void loadLocalAddresses() {

        try {

            var interfaces =
                    NetworkInterface
                            .getNetworkInterfaces();


            if (interfaces == null) {
                return;
            }


            for (
                    NetworkInterface networkInterface :
                    Collections.list(interfaces)
            ) {

                for (
                        InetAddress address :
                        Collections.list(
                                networkInterface
                                        .getInetAddresses()
                        )
                ) {

                    String localIp =
                            normalize(
                                    address.getHostAddress()
                            );


                    protectedAddresses.add(
                            localIp
                    );
                }
            }


        } catch (SocketException exception) {

            throw new IllegalStateException(
                    "Failed to read local network addresses",
                    exception
            );
        }
    }


    private String normalize(
            String ip
    ) {

        String value =
                ip.trim();


        int scopeIndex =
                value.indexOf('%');


        if (scopeIndex >= 0) {

            value =
                    value.substring(
                            0,
                            scopeIndex
                    );
        }


        return value;
    }
}