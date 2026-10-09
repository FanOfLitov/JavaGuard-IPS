package com.javaguard.ips.response.model;

import java.time.Duration;




public record ResponseDecision (
        boolean shouldBlock,
        Duration duration,
        String reason
){

    public static ResponseDecision ignore(){
        return new ResponseDecision(
                false,
                Duration.ZERO,
                "no automatic response"
        );
    }

    public static ResponseDecision block(
            Duration duration,
            String reason
    ){
        return new ResponseDecision(
                true,
                duration,
                reason
        );
    }
}
