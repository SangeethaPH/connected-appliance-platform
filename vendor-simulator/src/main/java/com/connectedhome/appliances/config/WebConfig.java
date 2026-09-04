package com.connectedhome.appliances.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String dashboardOrigin;

    public WebConfig(@Value("${app.cors.dashboard-origin:http://localhost:5173}") String dashboardOrigin) {
        this.dashboardOrigin = dashboardOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(dashboardOrigin)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS").allowedHeaders("*");
    }
}
