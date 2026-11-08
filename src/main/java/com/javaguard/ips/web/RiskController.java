package com.javaguard.ips.web;

import com.javaguard.ips.detection.risk.SourceRiskService;
import com.javaguard.ips.detection.risk.SourceRiskSnapshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final SourceRiskService riskService;


    public RiskController(
            SourceRiskService riskService
    ) {
        this.riskService = riskService;
    }


    @GetMapping
    public List<SourceRiskSnapshot> findAll() {
        return riskService.findAll();
    }


    @GetMapping("/{ip}")
    public SourceRiskSnapshot findByIp(
            @PathVariable
            String ip
    ) {
        return riskService.get(ip);
    }
}