package com.cartumio.gate.dto.request.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record EmailRequest(
    @NotBlank(message = "Subject is required") String subject,
    EmailUserRequest sender,
    @NotEmpty(message = "To is required") List<EmailUserRequest> to,
    String htmlContent)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
