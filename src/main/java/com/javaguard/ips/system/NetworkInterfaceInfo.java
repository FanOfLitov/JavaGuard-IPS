package com.javaguard.ips.system;

import java.util.List;

public record NetworkInterfaceInfo (
        String name,
        String displayName,
        boolean up,
        boolean loopback,
        boolean virtual,
        int mtu,
        List<String> addresses
){

}
