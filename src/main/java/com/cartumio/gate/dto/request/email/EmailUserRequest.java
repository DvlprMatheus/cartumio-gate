package com.cartumio.gate.dto.request.email;

import java.io.Serial;
import java.io.Serializable;

import com.cartumio.gate.domain.email.EmailUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailUserRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email address") String email)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public EmailUserRequest(EmailUser user) {
        this(user.getName(), user.getEmail());
    }
}
