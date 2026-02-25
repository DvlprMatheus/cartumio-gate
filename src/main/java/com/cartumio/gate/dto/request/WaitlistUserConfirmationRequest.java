package com.cartumio.gate.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

public record WaitlistUserConfirmationRequest(@NotBlank(message = "Token is required") String token)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
