package com.coupleapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// General application configuration beans
@Configuration
public class AppConfig {

    // RestTemplate for calling external APIs (Google Places, TripAdvisor)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
