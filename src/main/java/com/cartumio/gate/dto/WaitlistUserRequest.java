package com.cartumio.gate.dto;

import java.io.Serializable;

public record WaitlistUserRequest(
        String firstName,
        String lastName,
        String email) implements Serializable {
    private static final long serialVersionUID = 1L;
}
