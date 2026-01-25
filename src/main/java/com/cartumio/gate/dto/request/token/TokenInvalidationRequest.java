package com.cartumio.gate.dto.request.token;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;

public record TokenInvalidationRequest(
                @NotBlank(message = "Token is required") String token) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
}
