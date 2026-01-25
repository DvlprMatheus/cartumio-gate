package com.cartumio.gate.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);

    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.isConsumed = true AND t.expiresAt < :now")
    void deleteByIsConsumedTrueAndExpiresAtBefore(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") Instant now);
}
