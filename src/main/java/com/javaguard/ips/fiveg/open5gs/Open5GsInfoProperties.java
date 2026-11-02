package com.javaguard.ips.fiveg.open5gs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "javaguard.fiveg.open5gs")
public class Open5GsInfoProperties {

    private boolean enabled = false;

    private String smfBaseUrl = "http://127.0.0.4:9090";

    private long syncIntervalMs = 5000;

    private String amfBaseUrl = "http://127.0.0.5:9090";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }

    public String getAmfBaseUrl() {
        return amfBaseUrl;
    }

    public void setAmfBaseUrl(String amfBaseUrl) {
        this.amfBaseUrl = amfBaseUrl;
    }


    public String getSmfBaseUrl() {
        return smfBaseUrl;
    }

    public void setSmfBaseUrl(
            String smfBaseUrl
    ) {
        this.smfBaseUrl = smfBaseUrl;
    }


    public long getSyncIntervalMs() {
        return syncIntervalMs;
    }

    public void setSyncIntervalMs(
            long syncIntervalMs
    ) {
        this.syncIntervalMs = syncIntervalMs;
    }
}