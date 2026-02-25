package com.cartumio.gate.service.token;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.dto.response.token.TokenVerificationResponse;
import com.cartumio.gate.repository.TokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class TokenService {

  private final TokenRepository tokenRepository;

  private static final Duration DEFAULT_EXPIRATION = Duration.ofHours(24);
  private static final long CLEANUP_RETENTION_DAYS = 7;

  @Transactional
  public Token generateToken(
      TokenType tokenType, Duration expiration, Map<String, Object> metadata) {
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
    if (metadata != null) {
      token.setMetadata(new HashMap<>(metadata));
    }

    Token savedToken = tokenRepository.save(token);
    log.info(
        "Token generated | tokenType={}, expiresAt={}, hasMetadata={}",
        tokenType,
        expiresAt,
        metadata != null && !metadata.isEmpty());

    return savedToken;
  }

  @Transactional
  public Token generateToken(TokenType tokenType, Duration expiration) {
    return generateToken(tokenType, expiration, null);
  }

  @Transactional
  public TokenResponse generateToken(TokenType tokenType) {
    return new TokenResponse(generateToken(tokenType, DEFAULT_EXPIRATION, null));
  }

  @Transactional
  public TokenResponse generateToken(TokenType tokenType, Map<String, Object> metadata) {
    return new TokenResponse(generateToken(tokenType, DEFAULT_EXPIRATION, metadata));
  }

  @Transactional
  public TokenResponse generateToken(TokenType tokenType, String email) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("email", email);
    return new TokenResponse(generateToken(tokenType, DEFAULT_EXPIRATION, metadata));
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

  public TokenVerificationResponse validateTokenWithDetails(String token, TokenType type) {
    Optional<Token> tokenOpt = tokenRepository.findByTokenAndTokenType(token, type);

    if (tokenOpt.isEmpty()) {
      log.debug("Token not found | token={}, type={}", token, type);
      return new TokenVerificationResponse(false, false, false);
    }

    Token foundToken = tokenOpt.get();
    boolean expired = foundToken.isExpired();
    boolean consumed = foundToken.isConsumed();
    boolean valid = foundToken.isValid();

    log.debug(
        "Token validation details | token={}, type={}, valid={}, expired={}, consumed={}",
        token,
        type,
        valid,
        expired,
        consumed);

    return new TokenVerificationResponse(valid, expired, consumed);
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
  public void invalidateAllNonConsumedForEmail(TokenType tokenType, String email) {
    List<Token> tokens =
        tokenRepository.findByTokenTypeAndMetadataEmailAndIsConsumedFalse(tokenType, email);
    if (tokens.isEmpty()) {
      log.debug("No tokens found to invalidate | tokenType={}, email={}", tokenType, email);
      return;
    }
    tokens.forEach(Token::consume);
    tokenRepository.saveAll(tokens);
    log.info(
        "Tokens invalidated for email | tokenType={}, email={}, count={}",
        tokenType,
        email,
        tokens.size());
  }

  public Map<String, Object> getMetadataFromToken(String token, TokenType tokenType) {
    Optional<Token> tokenOpt = tokenRepository.findByTokenAndTokenType(token, tokenType);

    if (tokenOpt.isEmpty()) {
      log.debug("Token not found | token={}, type={}", token, tokenType);
      throw new IllegalArgumentException("Token not found");
    }

    Token foundToken = tokenOpt.get();
    Map<String, Object> metadata = foundToken.getMetadata();

    if (metadata == null) {
      log.debug("Token has no metadata | token={}, type={}", token, tokenType);
      return new HashMap<>();
    }

    log.debug(
        "Metadata retrieved from token | token={}, type={}, metadataKeys={}",
        token,
        tokenType,
        metadata.keySet());
    return metadata;
  }

  public String getEmailFromMetadata(Map<String, Object> metadata) {
    if (metadata == null || metadata.isEmpty()) {
      log.debug("Metadata is null or empty");
      throw new IllegalArgumentException("Metadata is null or empty");
    }

    Object emailObj = metadata.get("email");
    if (emailObj == null) {
      log.debug("Email not found in metadata | metadataKeys={}", metadata.keySet());
      throw new IllegalArgumentException("Email not found in metadata");
    }

    if (!(emailObj instanceof String email)) {
      log.debug("Email in metadata is not a string | emailType={}", emailObj.getClass().getName());
      throw new IllegalArgumentException("Email in metadata is not a string");
    }

    log.debug("Email extracted from metadata | email={}", email);
    return email;
  }

  @Transactional
  public void cleanupExpiredTokens() {
    Instant cutOff = Instant.now().minus(CLEANUP_RETENTION_DAYS, ChronoUnit.DAYS);
    tokenRepository.deleteByExpiresAtBefore(cutOff);
    log.info(
        "Token cleanup completed | cutOff={}, retentionDays={}", cutOff, CLEANUP_RETENTION_DAYS);
  }
}
