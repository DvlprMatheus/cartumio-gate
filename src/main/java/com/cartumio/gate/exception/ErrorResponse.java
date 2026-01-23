package com.cartumio.gate.exception;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
