package com.javaguard.ips.web.dto;

public record AddAllowlistEntryRequest(

        String ip,

        String description

) {
}