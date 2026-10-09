package com.javaguard.ips.response.model;
import java.time.Instant;
public record BlockRecord (
        String ip,
        String reason,
        Instant blockedAt,
        Instant expiresAt

)
{


}
