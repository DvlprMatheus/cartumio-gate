package com.cartumio.gate.config.ratelimit;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    private Rule defaultRule;
    private List<Rule> rules;

    @Getter
    @Setter
    public static class Rule {
        private String path;
        private int capacity;
        private Duration refill;
    }
}
