package com.javaguard.ips.detection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "javaguard.detection.behavior")
public class BehavioralDetectionProperties {

    private FloodRule udpFlood =
            new FloodRule(
                    Duration.ofSeconds(3),
                    300,
                    Duration.ofSeconds(30)
            );

    private FloodRule icmpFlood =
            new FloodRule(
                    Duration.ofSeconds(3),
                    100,
                    Duration.ofSeconds(30)
            );

    private HorizontalScan horizontalScan =
            new HorizontalScan(
                    Duration.ofSeconds(10),
                    15,
                    Duration.ofSeconds(30)
            );


    public FloodRule getUdpFlood() {
        return udpFlood;
    }

    public void setUdpFlood(FloodRule udpFlood) {
        this.udpFlood = udpFlood;
    }


    public FloodRule getIcmpFlood() {
        return icmpFlood;
    }

    public void setIcmpFlood(FloodRule icmpFlood) {
        this.icmpFlood = icmpFlood;
    }


    public HorizontalScan getHorizontalScan() {
        return horizontalScan;
    }

    public void setHorizontalScan(
            HorizontalScan horizontalScan
    ) {
        this.horizontalScan = horizontalScan;
    }


    public static class FloodRule {

        private Duration window;
        private int threshold;
        private Duration cooldown;


        public FloodRule() {
        }


        public FloodRule(
                Duration window,
                int threshold,
                Duration cooldown
        ) {
            this.window = window;
            this.threshold = threshold;
            this.cooldown = cooldown;
        }


        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }


        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }


        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(Duration cooldown) {
            this.cooldown = cooldown;
        }
    }


    public static class HorizontalScan {

        private Duration window;
        private int distinctDestinations;
        private Duration cooldown;


        public HorizontalScan() {
        }


        public HorizontalScan(
                Duration window,
                int distinctDestinations,
                Duration cooldown
        ) {
            this.window = window;
            this.distinctDestinations =
                    distinctDestinations;
            this.cooldown = cooldown;
        }


        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }


        public int getDistinctDestinations() {
            return distinctDestinations;
        }

        public void setDistinctDestinations(
                int distinctDestinations
        ) {
            this.distinctDestinations =
                    distinctDestinations;
        }


        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(Duration cooldown) {
            this.cooldown = cooldown;
        }
    }
}