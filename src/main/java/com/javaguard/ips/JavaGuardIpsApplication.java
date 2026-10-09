package com.javaguard.ips;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class JavaGuardIpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaGuardIpsApplication.class, args);


    }

}
