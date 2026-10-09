package com.javaguard.ips.web;

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


@RestController
@RequestMapping(
        "/api/v1/response"
)
public class ResponseController {

    private final ThreatResponseService
            responseService;


    public ResponseController(
            ThreatResponseService responseService
    ) {

        this.responseService =
                responseService;
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
}