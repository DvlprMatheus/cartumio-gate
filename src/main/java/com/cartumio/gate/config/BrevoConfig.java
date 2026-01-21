package com.cartumio.gate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BrevoConfig {
    
    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.base-url}")
    private String baseUrl;

    @Bean
    public WebClient brevoClient() {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("api-key", apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
