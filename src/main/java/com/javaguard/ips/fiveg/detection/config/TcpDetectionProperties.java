package com.javaguard.ips.detection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "javaguard.detection.tcp")
public class TcpDetectionProperties {

    private HalfOpen halfOpen =
            new HalfOpen();

    private ResetBurst resetBurst =
            new ResetBurst();

    private FailureRatio failureRatio =
            new FailureRatio();


    public HalfOpen getHalfOpen() {
        return halfOpen;
    }

    public void setHalfOpen(
            HalfOpen halfOpen
    ) {
        this.halfOpen = halfOpen;
    }


    public ResetBurst getResetBurst() {
        return resetBurst;
    }

    public void setResetBurst(
            ResetBurst resetBurst
    ) {
        this.resetBurst = resetBurst;
    }


    public FailureRatio getFailureRatio() {
        return failureRatio;
    }

    public void setFailureRatio(
            FailureRatio failureRatio
    ) {
        this.failureRatio = failureRatio;
    }


    public static class HalfOpen {

        private Duration window =
                Duration.ofSeconds(10);

        private int threshold = 100;

        private Duration cooldown =
                Duration.ofSeconds(30);


        public Duration getWindow() {
            return window;
        }

        public void setWindow(
                Duration window
        ) {
            this.window = window;
        }


        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(
                int threshold
        ) {
            this.threshold = threshold;
        }


        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(
                Duration cooldown
        ) {
            this.cooldown = cooldown;
        }
    }


    public static class ResetBurst {

        private Duration window =
                Duration.ofSeconds(10);

        private int threshold = 50;

        private Duration cooldown =
                Duration.ofSeconds(30);


        public Duration getWindow() {
            return window;
        }

        public void setWindow(
                Duration window
        ) {
            this.window = window;
        }


        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(
                int threshold
        ) {
            this.threshold = threshold;
        }


        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(
                Duration cooldown
        ) {
            this.cooldown = cooldown;
        }
    }


    public static class FailureRatio {

        private Duration window =
                Duration.ofSeconds(30);

        private int minimumFlows = 20;

        private double threshold = 0.80;

        private Duration cooldown =
                Duration.ofSeconds(30);


        public Duration getWindow() {
            return window;
        }

        public void setWindow(
                Duration window
        ) {
            this.window = window;
        }


        public int getMinimumFlows() {
            return minimumFlows;
        }

        public void setMinimumFlows(
                int minimumFlows
        ) {
            this.minimumFlows =
                    minimumFlows;
        }


        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(
                double threshold
        ) {
            this.threshold = threshold;
        }


        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(
                Duration cooldown
        ) {
            this.cooldown = cooldown;
        }
    }
}