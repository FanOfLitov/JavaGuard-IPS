package com.javaguard.ips.web;

import com.javaguard.ips.system.NetworkInterfaceInfo;
import com.javaguard.ips.system.NetworkInterfaceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network")
public class NetworkInterfaceController {
    private final NetworkInterfaceService networkInterfaceService;

    public NetworkInterfaceController(
            NetworkInterfaceService networkInterfaceService
    ){
        this.networkInterfaceService = networkInterfaceService;
    }

    @GetMapping("/interfaces")
    public List<NetworkInterfaceInfo> getInterfaces(){
        return networkInterfaceService.getInterfaces();
    }
}
