package com.javaguard.ips.system;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Service
public class NetworkInterfaceService {

    public List<NetworkInterfaceInfo> getInterfaces() {

        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            if (interfaces == null) {
                return List.of();
            }

            List<NetworkInterfaceInfo> result =
                    new ArrayList<>();

            while (interfaces.hasMoreElements()) {

                NetworkInterface networkInterface =
                        interfaces.nextElement();

                List<String> addresses =
                        Collections
                                .list(networkInterface.getInetAddresses())
                                .stream()
                                .map(InetAddress::getHostAddress)
                                .toList();

                NetworkInterfaceInfo info =
                        new NetworkInterfaceInfo(
                                networkInterface.getName(),
                                networkInterface.getDisplayName(),
                                networkInterface.isUp(),
                                networkInterface.isLoopback(),
                                networkInterface.isVirtual(),
                                networkInterface.getMTU(),
                                addresses
                        );

                result.add(info);
            }

            return List.copyOf(result);

        } catch (SocketException exception) {

            throw new IllegalStateException(
                    "Failed to read network interfaces",
                    exception
            );
        }
    }
}