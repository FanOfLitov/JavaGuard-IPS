package com.javaguard.ips.response.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@ConfigurationProperties(
        prefix = "javaguard.response"
)
public class ResponseProperties {

    private String mode =
            "logging";


    private boolean autoBlockEnabled =
            true;


    private List<String> protectedAddresses =
            new ArrayList<>(
                    List.of(
                            "127.0.0.1",
                            "::1",
                            "0:0:0:0:0:0:0:1"
                    )
            );


    public String getMode() {
        return mode;
    }


    public void setMode(
            String mode
    ) {
        this.mode = mode;
    }


    public boolean isAutoBlockEnabled() {
        return autoBlockEnabled;
    }


    public void setAutoBlockEnabled(
            boolean autoBlockEnabled
    ) {
        this.autoBlockEnabled =
                autoBlockEnabled;
    }


    public List<String> getProtectedAddresses() {
        return protectedAddresses;
    }


    public void setProtectedAddresses(
            List<String> protectedAddresses
    ) {
        this.protectedAddresses =
                protectedAddresses;
    }
}