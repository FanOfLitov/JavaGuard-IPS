package com.javaguard.ips.web;

import com.javaguard.ips.flow.tcp.TcpFlowSnapshot;
import com.javaguard.ips.flow.tcp.TcpFlowSummary;
import com.javaguard.ips.flow.tcp.TcpFlowTracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flows/tcp")
public class TcpFlowController {

    private final TcpFlowTracker flowTracker;


    public TcpFlowController(
            TcpFlowTracker flowTracker
    ) {
        this.flowTracker = flowTracker;
    }


    @GetMapping
    public List<TcpFlowSnapshot> findRecent(
            @RequestParam(
                    defaultValue = "100"
            )
            int limit
    ) {
        return flowTracker.findRecent(
                limit
        );
    }


    @GetMapping("/summary")
    public TcpFlowSummary summary() {
        return flowTracker.summary();
    }
}