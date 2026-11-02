package com.javaguard.ips.web;

import com.javaguard.ips.fiveg.policy.SlicePolicyService;
import com.javaguard.ips.fiveg.policy.model.SliceSecurityPolicy;
import com.javaguard.ips.web.dto.CreateSlicePolicyRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/5g/policies")
public class FiveGSlicePolicyController {

    private final SlicePolicyService policyService;

    public FiveGSlicePolicyController(
            SlicePolicyService policyService
    ) {
        this.policyService = policyService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SliceSecurityPolicy create(
            @RequestBody
            CreateSlicePolicyRequest request
    ) {
        return policyService.create(request);
    }


    @GetMapping
    public List<SliceSecurityPolicy> findAll() {
        return policyService.findAll();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID id
    ) {

        if (!policyService.delete(id)) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}