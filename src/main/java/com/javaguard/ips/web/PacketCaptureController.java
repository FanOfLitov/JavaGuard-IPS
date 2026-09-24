package com.javaguard.ips.web;

import com.javaguard.ips.capture.CapturePipelineService;
import com.javaguard.ips.capture.CapturePipelineStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/capture")
public class PacketCaptureController {

    private final CapturePipelineService capturePipelineService;

    public PacketCaptureController(
            CapturePipelineService capturePipelineService
    ) {
        this.capturePipelineService =
                capturePipelineService;
    }

    @PostMapping("/start/{interfaceName}")
    public CapturePipelineStatus start(
            @PathVariable String interfaceName
    ) {

        return capturePipelineService.start(
                interfaceName
        );
    }

    @PostMapping("/stop")
    public CapturePipelineStatus stop() {

        return capturePipelineService.stop();
    }

    @GetMapping("/status")
    public CapturePipelineStatus status() {

        return capturePipelineService.getStatus();
    }
}