package com.cartumio.gate.dto.response.token;

import java.io.Serial;
import java.io.Serializable;

public record TokenVerificationResponse(
        boolean valid,
        boolean expired,
        boolean consumed) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
