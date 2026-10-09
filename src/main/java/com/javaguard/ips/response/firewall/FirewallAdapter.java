package com.javaguard.ips.response.firewall;

public interface FirewallAdapter {
    void block(String ip);
    void unblock(String ip);
}
