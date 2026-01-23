package com.cartumio.gate.config.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {
    
    private String apiKey;
    private String baseUrl;
    private Sender sender;

    @Getter
    @Setter
    public static class Sender {
        private String name;
        private String email;
    }
}
