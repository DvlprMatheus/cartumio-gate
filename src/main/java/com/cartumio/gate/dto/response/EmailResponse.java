package com.cartumio.gate.dto.response;

import java.io.Serial;
import java.io.Serializable;

public record EmailResponse(String messageId) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
