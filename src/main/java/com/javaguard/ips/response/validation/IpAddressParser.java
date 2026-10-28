package com.javaguard.ips.response.validation;

import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


@Component
public class IpAddressParser {

    public ParsedIpAddress parse(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "IP address must not be empty"
            );
        }


        String ip =
                value.trim();


        if (ip.contains("%")) {

            throw new IllegalArgumentException(
                    "IPv6 zone identifiers are not supported: "
                            + ip
            );
        }


        if (ip.contains(":")) {

            return parseIpv6(ip);
        }


        return parseIpv4(ip);
    }


    private ParsedIpAddress parseIpv4(
            String ip
    ) {

        String[] parts =
                ip.split("\\.", -1);


        if (parts.length != 4) {

            throw new IllegalArgumentException(
                    "Invalid IPv4 address: "
                            + ip
            );
        }


        int[] numbers =
                new int[4];


        for (
                int i = 0;
                i < parts.length;
                i++
        ) {

            String part =
                    parts[i];


            if (
                    part.isEmpty()
                            ||
                            !part.chars()
                                    .allMatch(
                                            Character::isDigit
                                    )
            ) {

                throw new IllegalArgumentException(
                        "Invalid IPv4 address: "
                                + ip
                );
            }


            try {

                numbers[i] =
                        Integer.parseInt(part);

            } catch (
                    NumberFormatException exception
            ) {

                throw new IllegalArgumentException(
                        "Invalid IPv4 address: "
                                + ip
                );
            }


            if (
                    numbers[i] < 0
                            ||
                            numbers[i] > 255
            ) {

                throw new IllegalArgumentException(
                        "Invalid IPv4 address: "
                                + ip
                );
            }
        }


        String normalized =
                numbers[0]
                        + "."
                        + numbers[1]
                        + "."
                        + numbers[2]
                        + "."
                        + numbers[3];


        return new ParsedIpAddress(
                normalized,
                false
        );
    }


    private ParsedIpAddress parseIpv6(
            String ip
    ) {

        /*
         * До InetAddress допускаем только символы,
         * которые могут входить в IP literal.
         *
         * Это принципиально важно, потому что дальше
         * адрес попадёт в nftables script.
         */
        if (
                !ip.matches(
                        "[0-9a-fA-F:.]+"
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 address: "
                            + ip
            );
        }


        try {

            InetAddress address =
                    InetAddress.getByName(ip);


            if (
                    !(address instanceof Inet6Address)
            ) {

                throw new IllegalArgumentException(
                        "Invalid IPv6 address: "
                                + ip
                );
            }


            return new ParsedIpAddress(
                    address.getHostAddress(),
                    true
            );


        } catch (
                UnknownHostException exception
        ) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 address: "
                            + ip,
                    exception
            );
        }
    }


    public record ParsedIpAddress(

            String value,

            boolean ipv6

    ) {
    }
}