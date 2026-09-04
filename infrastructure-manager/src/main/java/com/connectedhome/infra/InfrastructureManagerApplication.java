package com.connectedhome.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InfrastructureManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfrastructureManagerApplication.class, args);
    }
}
