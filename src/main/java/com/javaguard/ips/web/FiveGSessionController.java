package com.javaguard.ips.web;

import com.javaguard.ips.fiveg.model.PduSessionContext;
import com.javaguard.ips.fiveg.session.FiveGSessionService;

import com.javaguard.ips.web.dto.RegisterPduSessionRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/5g/sessions")
public class FiveGSessionController {

    private final FiveGSessionService
            sessionService;


    public FiveGSessionController(
            FiveGSessionService sessionService
    ) {

        this.sessionService =
                sessionService;
    }


    @PostMapping
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public PduSessionContext register(
            @RequestBody
            RegisterPduSessionRequest request
    ) {

        return sessionService.register(
                request
        );
    }


    @GetMapping
    public List<PduSessionContext> findAll() {

        return sessionService.findAll();
    }


    @GetMapping("/lookup")
    public ResponseEntity<PduSessionContext>
    findByUeIp(@RequestParam String ueIp
    ) {

        return sessionService
                .findByUeIp(ueIp)
                .map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .notFound()
                                        .build()
                );
    }


    @GetMapping("/tunnel")
    public ResponseEntity<PduSessionContext> findByTunnel(
            @RequestParam String endpointIp,
            @RequestParam long teid
    ) {

        return sessionService
                .findByTunnel(
                        endpointIp,
                        teid
                )
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {

        boolean removed = sessionService.remove(id);


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