package com.javaguard.ips.web.dto;

public record BlockIpRequest (
        String ip,
        long durationMinutes,
        String reason
){
}
