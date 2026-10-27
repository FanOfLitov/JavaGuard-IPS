package com.javaguard.ips.web;

import com.javaguard.ips.response.ProtectedAddressService;
import com.javaguard.ips.response.ThreatResponseService;
import com.javaguard.ips.response.model.BlockRecord;
import com.javaguard.ips.web.dto.BlockIpRequest;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.javaguard.ips.response.AllowlistService;
import com.javaguard.ips.response.model.AllowlistEntry;
import com.javaguard.ips.web.dto.AddAllowlistEntryRequest;

@RestController
@RequestMapping(
        "/api/v1/response"
)
public class ResponseController {

    private final ThreatResponseService responseService;

    private final ProtectedAddressService protectedAddressService;


    private final AllowlistService allowlistService;


    public ResponseController(
            ThreatResponseService responseService,
            ProtectedAddressService protectedAddressService,
            AllowlistService allowlistService
    ) {

        this.responseService = responseService;

        this.protectedAddressService = protectedAddressService;
        this.allowlistService=allowlistService;
    }

    @GetMapping("/protected")
    public Set<String> protectedAddresses() {

        return protectedAddressService
                .getProtectedAddresses();
    }


    @PostMapping("/block")
    public BlockRecord block(
            @RequestBody
            BlockIpRequest request
    ) {

        return responseService.block(

                request.ip(),

                Duration.ofMinutes(
                        request.durationMinutes()
                ),

                request.reason()
        );
    }


    @DeleteMapping("/block")
    public ResponseEntity<Void> unblock(
            @RequestParam
            String ip
    ) {

        boolean removed =
                responseService.unblock(ip);


        if (!removed) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity
                .noContent()
                .build();
    }


    @GetMapping("/blocked")
    public List<BlockRecord> blocked() {

        return responseService
                .getBlockedIps();
    }


    @GetMapping("/blocked/count")
    public Map<String, Long> blockedCount() {

        return Map.of(
                "count",
                responseService
                        .getBlockedCount()
        );
    }


    @GetMapping("/allowlist")
    public List<AllowlistEntry> allowlist() {

        return allowlistService
                .getAll();
    }


    @PostMapping("/allowlist")
    public AllowlistEntry addAllowlistEntry(
            @RequestBody
            AddAllowlistEntryRequest request
    ) {

        return allowlistService.add(
                request.ip(),
                request.description()
        );
    }


    @DeleteMapping("/allowlist")
    public ResponseEntity<Void> removeAllowlistEntry(
            @RequestParam
            String ip
    ) {

        boolean removed =
                allowlistService.remove(ip);


        if (!removed) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity
                .noContent()
                .build();
    }
}