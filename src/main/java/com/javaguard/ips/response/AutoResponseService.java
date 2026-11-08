package com.javaguard.ips.response;

import com.javaguard.ips.detection.model.SecurityEvent;
import com.javaguard.ips.detection.risk.RiskProperties;
import com.javaguard.ips.detection.risk.SourceRiskService;
import com.javaguard.ips.detection.risk.SourceRiskSnapshot;
import com.javaguard.ips.response.model.BlockSource;
import com.javaguard.ips.response.model.ResponseDecision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AutoResponseService {

    private final ResponsePolicyService policyService;

    private final ThreatResponseService responseService;

    private final SourceRiskService riskService;

    private final RiskProperties riskProperties;


    @Autowired
    public AutoResponseService(
            ResponsePolicyService policyService,
            ThreatResponseService responseService,
            SourceRiskService riskService,
            RiskProperties riskProperties
    ) {
        this.policyService = policyService;
        this.responseService = responseService;
        this.riskService = riskService;
        this.riskProperties = riskProperties;
    }


    /*
     * Оставляем этот конструктор,
     * чтобы старые unit tests не пришлось
     * переписывать все сразу.
     */
    public AutoResponseService(
            ResponsePolicyService policyService,
            ThreatResponseService responseService
    ) {
        this.policyService = policyService;
        this.responseService = responseService;
        this.riskService = null;
        this.riskProperties = null;
    }


    public void handle(
            SecurityEvent event
    ) {

        if (event == null) {
            return;
        }

        SourceRiskSnapshot risk =
                recordRisk(event);

        ResponseDecision decision =
                policyService.evaluate(event);

        if (!decision.shouldBlock()) {
            return;
        }

        Duration duration =
                decision.duration();

        String reason =
                decision.reason();


        if (shouldEscalate(risk)) {

            Duration escalated =
                    riskProperties
                            .getEscalatedBlockDuration();

            if (escalated.compareTo(duration) > 0) {
                duration = escalated;
            }

            reason =
                    reason
                            + "; correlated risk score="
                            + risk.score()
                            + " ("
                            + risk.level()
                            + ")";
        }


        responseService.block(
                event.sourceIp(),
                duration,
                reason,
                BlockSource.AUTOMATIC
        );
    }


    private SourceRiskSnapshot recordRisk(
            SecurityEvent event
    ) {

        if (riskService == null) {
            return null;
        }

        if (riskProperties == null
                || !riskProperties.isEnabled()) {

            return null;
        }

        return riskService.record(event);
    }


    private boolean shouldEscalate(
            SourceRiskSnapshot risk
    ) {

        if (risk == null
                || riskProperties == null) {

            return false;
        }

        return risk.score()
                >= riskProperties.getBlockThreshold();
    }
}