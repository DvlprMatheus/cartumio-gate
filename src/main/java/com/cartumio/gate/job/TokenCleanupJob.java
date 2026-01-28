package com.cartumio.gate.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cartumio.gate.service.token.TokenService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class TokenCleanupJob {

    private final TokenService tokenService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job");
        try {
            tokenService.cleanupExpiredTokens();
            log.info("Token cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Error during token cleanup job", e);
        }
    }
}
