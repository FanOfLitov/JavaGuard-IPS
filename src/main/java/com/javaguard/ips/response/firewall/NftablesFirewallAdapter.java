package com.javaguard.ips.response.firewall;

import com.javaguard.ips.response.validation.IpAddressParser;
import com.javaguard.ips.response.validation.IpAddressParser.ParsedIpAddress;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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


    private final IpAddressParser
            ipAddressParser;


    public NftablesFirewallAdapter(
            IpAddressParser ipAddressParser
    ) {

        this.ipAddressParser =
                ipAddressParser;
    }


    @PostConstruct
    public void initialize() {

        verifyNftablesAvailable();



        if (tableExists()) {

            log.info(
                    "Removing existing JavaGuard nftables table"
            );


            runCommand(
                    List.of(
                            "nft",
                            "delete",
                            "table",
                            "inet",
                            TABLE
                    ),
                    true
            );
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
                    type filter hook input priority filter;
                    policy accept;
                }

                add rule inet javaguard input \
                    ip saddr @blocked_ipv4 counter drop

                add rule inet javaguard input \
                    ip6 saddr @blocked_ipv6 counter drop
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

        ParsedIpAddress address =
                ipAddressParser.parse(ip);


        String targetSet =
                address.ipv6()
                        ? IPV6_SET
                        : IPV4_SET;


        String script =
                "add element inet "
                        + TABLE
                        + " "
                        + targetSet
                        + " { "
                        + address.value()
                        + " }\n";


        runScript(script);


        log.warn(
                "Firewall block applied: ip={}, set={}",
                address.value(),
                targetSet
        );
    }


    @Override
    public void unblock(
            String ip
    ) {

        ParsedIpAddress address =
                ipAddressParser.parse(ip);


        String targetSet =
                address.ipv6()
                        ? IPV6_SET
                        : IPV4_SET;


        String script =
                "delete element inet "
                        + TABLE
                        + " "
                        + targetSet
                        + " { "
                        + address.value()
                        + " }\n";


        runScript(script);


        log.info(
                "Firewall block removed: ip={}, set={}",
                address.value(),
                targetSet
        );
    }


    private void verifyNftablesAvailable() {

        CommandResult result =
                runCommand(
                        List.of(
                                "nft",
                                "--version"
                        ),
                        false
                );


        if (result.exitCode() != 0) {

            throw new IllegalStateException(
                    "nftables is not available: "
                            + result.output()
            );
        }


        log.info(
                "nftables detected: {}",
                result.output().trim()
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
                    var stdin =
                            process.getOutputStream()
            ) {

                stdin.write(
                        script.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }


            String output =
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
                        "nftables command failed: "
                                + output.trim()
                );
            }


        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Failed to execute nftables",
                    exception
            );


        } catch (
                InterruptedException exception
        ) {

            Thread
                    .currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "nftables command was interrupted",
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
                                + String.join(
                                " ",
                                command
                        )
                                + ": "
                                + output.trim()
                );
            }


            return new CommandResult(
                    exitCode,
                    output
            );


        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Failed to execute command: "
                            + command,
                    exception
            );


        } catch (
                InterruptedException exception
        ) {

            Thread
                    .currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "Command was interrupted",
                    exception
            );
        }
    }


    private record CommandResult(

            int exitCode,

            String output

    ) {
    }
}