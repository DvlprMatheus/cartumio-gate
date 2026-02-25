package com.cartumio.gate.repository;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

  Optional<Token> findByToken(String token);

  boolean existsByToken(String token);

  Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);

  @Query(
      value =
          """
            SELECT * FROM tokens
            WHERE token_type = CAST(:#{#type.name()} AS text)
              AND is_consumed = false
              AND metadata->>'email' = :email
            """,
      nativeQuery = true)
  List<Token> findByTokenTypeAndMetadataEmailAndIsConsumedFalse(
      @Param("type") TokenType type, @Param("email") String email);

  @Modifying
  @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
  void deleteByExpiresAtBefore(@Param("now") Instant now);
}
