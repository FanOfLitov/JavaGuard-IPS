package com.javaguard.ips.persistence.specification;


import com.javaguard.ips.detection.model.Severity;
import com.javaguard.ips.detection.model.ThreatType;
import com.javaguard.ips.persistence.entity.SecurityAlertEntity;

import org.springframework.data.jpa.domain.Specification;

public class SecurityAlertSpecifications {

    private SecurityAlertSpecifications(){}

    public static Specification<SecurityAlertEntity> hasType( ThreatType type){
        if(type==null){
            return Specification.unrestricted();
        }

        return (root, query, criterialBuilder)->
                criterialBuilder.equal(
                        root.get("type"),
                        type
                );
    }

    public static Specification<SecurityAlertEntity> hasSeverity( Severity severity){
        if(severity == null) return Specification.unrestricted();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("severity"),
                        severity
                );

    }

    public static Specification<SecurityAlertEntity> hasSourceIp(String sourceIp){
        if(sourceIp ==null || sourceIp.isBlank()){
            return Specification.unrestricted();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sourceIp"), sourceIp);
    }

    public static Specification<SecurityAlertEntity> hasDestinationIp(String destinationIp){
        if(destinationIp == null || destinationIp.isBlank()){
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder)-> criteriaBuilder.equal(root.get("destinationIp"), destinationIp);
    }
}
