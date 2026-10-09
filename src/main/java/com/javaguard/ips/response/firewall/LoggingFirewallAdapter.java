package com.javaguard.ips.response.firewall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class LoggingFirewallAdapter
        implements FirewallAdapter {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LoggingFirewallAdapter.class
            );


    @Override
    public void block(String ip) {

        log.warn(
                "SIMULATED FIREWALL: blocking IP {}",
                ip
        );
    }


    @Override
    public void unblock(String ip) {

        log.info(
                "SIMULATED FIREWALL: unblocking IP {}",
                ip
        );
    }
}