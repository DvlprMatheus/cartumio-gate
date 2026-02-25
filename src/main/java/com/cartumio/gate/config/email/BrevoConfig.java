package com.cartumio.gate.config.email;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@AllArgsConstructor
@Configuration
public class BrevoConfig {

  private final BrevoProperties properties;

  @Bean
  public RestClient brevoRestClient() {
    return RestClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("api-key", properties.getApiKey())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
