package com.javaguard.ips.response.model;

import java.time.Instant;
public record AllowlistEntry (
    String ip,
    String description,
    Instant createdAt
){
}
