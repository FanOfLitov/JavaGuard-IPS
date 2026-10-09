package com.javaguard.ips.response;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.response.model.ResponseDecision;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ResponsePolicyService {

    public ResponseDecision evaluate(SecurityEvent event){

        if(event==null){
            return ResponseDecision.ignore();
        }

        String sourceIp=event.sourceIp();

        if(isProtectedAddress(sourceIp)){
            return ResponseDecision.ignore();
        }

        if(event.type()==ThreatType.PORT_SCAN){
            return ResponseDecision.block(
                    Duration.ofMinutes(5),
                    "Automatic response to PORT_SCAN"
            );
        }

        if(event.type() == ThreatType.SYN_FLOOD){
            return ResponseDecision.block(
                    Duration.ofMinutes(15),
                    "Automatic response to SYN_FLOOD"
            );
        }

        return ResponseDecision.ignore();
    }

    private boolean isProtectedAddress(
            String ip
    ){
        if(ip==null)return true;

        return ip.equals("127.0.0.1")
                || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1");
    }
}
