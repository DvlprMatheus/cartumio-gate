package com.cartumio.gate.service.token;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.repository.TokenRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    private static final Duration DEFAULT_EXPIRATION = Duration.ofHours(24);

    @Transactional
    public Token generateToken(TokenType tokenType, Duration expiration) {
        String tokenValue = UUID.randomUUID().toString();

        while (tokenRepository.existsByToken(tokenValue)) {
            tokenValue = UUID.randomUUID().toString();
        }

        Instant expiresAt = Instant.now().plus(expiration != null ? expiration : DEFAULT_EXPIRATION);

        Token token = new Token();
        token.setToken(tokenValue);
        token.setTokenType(tokenType);
        token.setExpiresAt(expiresAt);
        token.setConsumed(false);

        Token savedToken = tokenRepository.save(token);
        log.info("Token generated | tokenType={}, expiresAt={}", tokenType, expiresAt);

        return savedToken;
    }

    @Transactional
    public TokenResponse generateToken(TokenType tokenType) {
        return new TokenResponse(generateToken(tokenType, DEFAULT_EXPIRATION));
    }

    public boolean validateToken(String token, TokenType type) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndTokenType(token, type);

        if (tokenOpt.isEmpty()) {
            log.debug("Token not found | token={}, type={}", token, type);
            return false;
        }

        Token foundToken = tokenOpt.get();
        boolean isValid = foundToken.isValid();

        log.debug("Token validation | token={}, type={}, valid={}", token, type, isValid);
        return isValid;
    }

    public TokenValidationResult validateTokenWithDetails(String token, TokenType type) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenAndTokenType(token, type);

        if (tokenOpt.isEmpty()) {
            log.debug("Token not found | token={}, type={}", token, type);
            return new TokenValidationResult(false, false, false);
        }

        Token foundToken = tokenOpt.get();
        boolean expired = foundToken.isExpired();
        boolean consumed = foundToken.isConsumed();
        boolean valid = foundToken.isValid();

        log.debug("Token validation details | token={}, type={}, valid={}, expired={}, consumed={}",
                token, type, valid, expired, consumed);

        return new TokenValidationResult(valid, expired, consumed);
    }

    public static class TokenValidationResult {
        private final boolean valid;
        private final boolean expired;
        private final boolean consumed;

        public TokenValidationResult(boolean valid, boolean expired, boolean consumed) {
            this.valid = valid;
            this.expired = expired;
            this.consumed = consumed;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isExpired() {
            return expired;
        }

        public boolean isConsumed() {
            return consumed;
        }
    }

    @Transactional
    public boolean invalidateToken(String token) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.debug("Token not found for invalidation | token={}", token);
            return false;
        }

        Token foundToken = tokenOpt.get();

        if (foundToken.isConsumed()) {
            log.debug("Token already consumed | token={}", token);
            return false;
        }

        foundToken.consume();
        tokenRepository.save(foundToken);
        log.info("Token invalidated | token={}", token);

        return true;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();

        tokenRepository.deleteByExpiresAtBefore(now);

        tokenRepository.deleteByIsConsumedTrueAndExpiresAtBefore(now);

        log.info("Token cleanup completed | timestamp={}", now);
    }
}
