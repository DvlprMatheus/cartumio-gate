package com.cartumio.gate.exception;

public class EmailFailedException extends RuntimeException {

  public EmailFailedException(String message) {
    super(message);
  }

  public EmailFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
