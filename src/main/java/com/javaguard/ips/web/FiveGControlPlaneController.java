package com.javaguard.ips.web;

import com.javaguard.ips.fiveg.open5gs.ControlPlaneSessionRegistry;
import com.javaguard.ips.fiveg.open5gs.Open5GsContextSyncService;
import com.javaguard.ips.fiveg.open5gs.Open5GsSyncStatus;

import com.javaguard.ips.fiveg.open5gs.model.ControlPlanePduSession;

import com.javaguard.ips.fiveg.reconciliation.FiveGReconciliationSnapshot;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/5g/control-plane"
)
public class FiveGControlPlaneController {

    private final Open5GsContextSyncService
            syncService;

    private final ControlPlaneSessionRegistry
            registry;


    public FiveGControlPlaneController(
            Open5GsContextSyncService syncService,
            ControlPlaneSessionRegistry registry
    ) {
        this.syncService = syncService;
        this.registry = registry;
    }


    @GetMapping("/status")
    public Open5GsSyncStatus status() {
        return syncService.status();
    }


    @GetMapping("/sessions")
    public List<ControlPlanePduSession> sessions() {
        return registry.findAll();
    }


    @GetMapping("/reconciliation")
    public ResponseEntity<FiveGReconciliationSnapshot>
    reconciliation() {

        FiveGReconciliationSnapshot snapshot =
                syncService
                        .getLastReconciliation();

        if (snapshot == null) {
            return ResponseEntity
                    .noContent()
                    .build();
        }

        return ResponseEntity.ok(
                snapshot
        );
    }


    @PostMapping("/sync")
    public Open5GsSyncStatus sync() {
        return syncService.syncNow();
    }
}