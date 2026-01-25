package com.cartumio.gate.dto.request.token;

import java.io.Serial;
import java.io.Serializable;

import com.cartumio.gate.domain.token.TokenType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TokenVerificationRequest(
                @NotBlank(message = "Token is required") String token,
                @NotNull(message = "Token type is required") TokenType tokenType) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
}