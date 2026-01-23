package com.cartumio.gate.dto.request;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record WaitlistUserRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email address") String email)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
