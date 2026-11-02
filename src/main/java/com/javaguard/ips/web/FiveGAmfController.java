package com.javaguard.ips.web;

import com.javaguard.ips.fiveg.open5gs.AmfUeRegistry;
import com.javaguard.ips.fiveg.open5gs.GnbRegistry;
import com.javaguard.ips.fiveg.open5gs.Open5GsAmfSyncService;
import com.javaguard.ips.fiveg.open5gs.Open5GsAmfSyncStatus;

import com.javaguard.ips.fiveg.open5gs.model.AmfUeContext;
import com.javaguard.ips.fiveg.open5gs.model.GnbContext;

import com.javaguard.ips.fiveg.reconciliation
        .FiveGControlPlaneConsistencySnapshot;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/5g/amf")
public class FiveGAmfController {

    private final Open5GsAmfSyncService syncService;
    private final AmfUeRegistry ueRegistry;
    private final GnbRegistry gnbRegistry;


    public FiveGAmfController(
            Open5GsAmfSyncService syncService,
            AmfUeRegistry ueRegistry,
            GnbRegistry gnbRegistry
    ) {
        this.syncService = syncService;
        this.ueRegistry = ueRegistry;
        this.gnbRegistry = gnbRegistry;
    }


    @GetMapping("/status")
    public Open5GsAmfSyncStatus status() {
        return syncService.status();
    }


    @GetMapping("/ues")
    public List<AmfUeContext> ues() {
        return ueRegistry.findAll();
    }


    @GetMapping("/gnbs")
    public List<GnbContext> gnbs() {
        return gnbRegistry.findAll();
    }


    @GetMapping("/consistency")
    public ResponseEntity<FiveGControlPlaneConsistencySnapshot>
    consistency() {

        FiveGControlPlaneConsistencySnapshot snapshot =
                syncService.getLastConsistency();

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
    public Open5GsAmfSyncStatus sync() {
        return syncService.syncNow();
    }
}