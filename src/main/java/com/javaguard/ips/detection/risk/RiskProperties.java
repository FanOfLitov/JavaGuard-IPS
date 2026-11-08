package com.javaguard.ips.detection.risk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "javaguard.risk")
public class RiskProperties {

    private boolean enabled = true;

    private int maxScore = 100;

    private int blockThreshold = 80;

    private int decayPointsPerMinute = 10;

    private int diversityBonus = 10;

    private Duration stateTtl =
            Duration.ofHours(1);

    private Duration escalatedBlockDuration =
            Duration.ofMinutes(30);


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }


    public int getBlockThreshold() {
        return blockThreshold;
    }

    public void setBlockThreshold(
            int blockThreshold
    ) {
        this.blockThreshold = blockThreshold;
    }


    public int getDecayPointsPerMinute() {
        return decayPointsPerMinute;
    }

    public void setDecayPointsPerMinute(
            int decayPointsPerMinute
    ) {
        this.decayPointsPerMinute =
                decayPointsPerMinute;
    }


    public int getDiversityBonus() {
        return diversityBonus;
    }

    public void setDiversityBonus(
            int diversityBonus
    ) {
        this.diversityBonus = diversityBonus;
    }


    public Duration getStateTtl() {
        return stateTtl;
    }

    public void setStateTtl(
            Duration stateTtl
    ) {
        this.stateTtl = stateTtl;
    }


    public Duration getEscalatedBlockDuration() {
        return escalatedBlockDuration;
    }

    public void setEscalatedBlockDuration(
            Duration escalatedBlockDuration
    ) {
        this.escalatedBlockDuration =
                escalatedBlockDuration;
    }
}