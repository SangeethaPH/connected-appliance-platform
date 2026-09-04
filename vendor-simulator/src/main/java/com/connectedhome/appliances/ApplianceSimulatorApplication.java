package com.connectedhome.appliances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ApplianceSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplianceSimulatorApplication.class, args);
    }
}
