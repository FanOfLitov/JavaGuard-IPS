package com.javaguard.ips.response.firewall;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition
        .ConditionalOnProperty;

import org.springframework.stereotype.Component;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.List;


@Component

@ConditionalOnProperty(
        prefix = "javaguard.response",
        name = "mode",
        havingValue = "nftables"
)
public class NftablesFirewallAdapter
        implements FirewallAdapter {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NftablesFirewallAdapter.class
            );


    private static final String TABLE =
            "javaguard";

    private static final String IPV4_SET =
            "blocked_ipv4";

    private static final String IPV6_SET =
            "blocked_ipv6";


    @PostConstruct
    public void initialize() {

        if (tableExists()) {

            log.info(
                    "JavaGuard nftables table already exists"
            );

            return;
        }


        log.info(
                "Creating JavaGuard nftables infrastructure"
        );


        String script = """
                add table inet javaguard

                add set inet javaguard blocked_ipv4 {
                    type ipv4_addr;
                }

                add set inet javaguard blocked_ipv6 {
                    type ipv6_addr;
                }

                add chain inet javaguard input {
                    type filter hook input priority 0;
                    policy accept;
                }

                add rule inet javaguard input \
                    ip saddr @blocked_ipv4 drop

                add rule inet javaguard input \
                    ip6 saddr @blocked_ipv6 drop
                """;


        runScript(script);


        log.info(
                "JavaGuard nftables infrastructure created"
        );
    }


    @Override
    public void block(
            String ip
    ) {

        IpAddress address =
                parseIp(ip);


        String set =
                address.ipv6()
                        ? IPV6_SET
                        : IPV4_SET;


        String script =
                "add element inet "
                        + TABLE
                        + " "
                        + set
                        + " { "
                        + address.value()
                        + " }\n";


        runScript(script);


        log.warn(
                "Firewall block applied: ip={}",
                address.value()
        );
    }


    @Override
    public void unblock(
            String ip
    ) {

        IpAddress address =
                parseIp(ip);


        String set =
                address.ipv6()
                        ? IPV6_SET
                        : IPV4_SET;


        String script =
                "delete element inet "
                        + TABLE
                        + " "
                        + set
                        + " { "
                        + address.value()
                        + " }\n";


        runScript(script);


        log.info(
                "Firewall block removed: ip={}",
                address.value()
        );
    }


    private boolean tableExists() {

        CommandResult result =
                runCommand(
                        List.of(
                                "nft",
                                "list",
                                "table",
                                "inet",
                                TABLE
                        ),
                        false
                );


        return result.exitCode() == 0;
    }


    private void runScript(
            String script
    ) {

        try {

            Process process =
                    new ProcessBuilder(
                            "nft",
                            "-f",
                            "-"
                    )
                            .redirectErrorStream(true)
                            .start();


            try (
                    var output =
                            process.getOutputStream()
            ) {

                output.write(
                        script.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }


            String commandOutput =
                    new String(
                            process
                                    .getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );


            int exitCode =
                    process.waitFor();


            if (exitCode != 0) {

                throw new IllegalStateException(
                        "nft command failed: "
                                + commandOutput
                );
            }


        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to execute nft command",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread
                    .currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "nft command was interrupted",
                    exception
            );
        }
    }


    private CommandResult runCommand(
            List<String> command,
            boolean failOnError
    ) {

        try {

            Process process =
                    new ProcessBuilder(command)
                            .redirectErrorStream(true)
                            .start();


            String output =
                    new String(
                            process
                                    .getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );


            int exitCode =
                    process.waitFor();


            if (
                    failOnError
                            &&
                            exitCode != 0
            ) {

                throw new IllegalStateException(
                        "Command failed: "
                                + output
                );
            }


            return new CommandResult(
                    exitCode,
                    output
            );


        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to execute command",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread
                    .currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "Command was interrupted",
                    exception
            );
        }
    }


    private IpAddress parseIp(
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
                    "IPv6 zone identifiers are not supported"
            );
        }


        if (ip.contains(":")) {

            return new IpAddress(
                    ip,
                    true
            );
        }


        String[] parts =
                ip.split(
                        "\\.",
                        -1
                );


        if (parts.length != 4) {

            throw new IllegalArgumentException(
                    "Invalid IPv4 address: "
                            + ip
            );
        }


        for (String part : parts) {

            try {

                int number =
                        Integer.parseInt(part);


                if (
                        number < 0
                                ||
                                number > 255
                ) {

                    throw new IllegalArgumentException(
                            "Invalid IPv4 address: "
                                    + ip
                    );
                }

            } catch (NumberFormatException exception) {

                throw new IllegalArgumentException(
                        "Invalid IPv4 address: "
                                + ip
                );
            }
        }


        return new IpAddress(
                ip,
                false
        );
    }


    private record IpAddress(
            String value,
            boolean ipv6
    ) {
    }


    private record CommandResult(
            int exitCode,
            String output
    ) {
    }
}