package com.cartumio.gate.dto.response.token;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;

public record TokenResponse(
        String token,
        Instant expiresAt,
        TokenType tokenType) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public TokenResponse(Token token) {
        this(token.getToken(), token.getExpiresAt(), token.getTokenType());
    }
}
