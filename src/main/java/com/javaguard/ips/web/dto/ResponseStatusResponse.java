package com.javaguard.ips.web.dto;

public record ResponseStatusResponse(

        String mode,

        boolean autoBlockEnabled,

        long activeBlocks,

        int allowlistedAddresses,

        int protectedAddresses

) {
}