package com.cartumio.gate.service;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;
import com.cartumio.gate.service.email.ConfirmationEmailService;
import com.cartumio.gate.service.token.TokenService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class WaitlistUserService {

  private final WaitlistUserRepository waitlistUserRepository;
  private final ConfirmationEmailService confirmationEmailService;
  private final SystemLocaleService systemLocaleService;
  private final TokenService tokenService;

  @Transactional
  public void createOrResendConfirmationEmail(WaitlistUserRequest request, String acceptLanguage) {
    log.info("Creating or resending confirmation email | email={}", request.email());
    Optional<WaitlistUser> existing = this.waitlistUserRepository.findByEmail(request.email());
    boolean isResend = existing.isPresent();

    if (isResend && existing.get().isConfirmed()) {
      log.warn("Waitlist user already exists and is confirmed | email={}", request.email());
      throw new EntityExistsException("Waitlist user already exists and is confirmed");
    }

    SystemLocale systemLocale = systemLocaleService.findActiveByCode(getLocaleCode(acceptLanguage));
    log.info(
        "System locale found | code={}, language={}",
        systemLocale.getCode(),
        systemLocale.getLanguage());

    WaitlistUser waitlistUser = existing.orElseGet(() -> createWaitlistUser(request, systemLocale));

    if (isResend) {
      log.info("Resending confirmation email to existing user | email={}", request.email());
    }

    confirmationEmailService.sendConfirmationEmail(
        waitlistUser.getFirstName(),
        waitlistUser.getLastName(),
        waitlistUser.getEmail(),
        systemLocale.getCode());
  }

  private WaitlistUser createWaitlistUser(WaitlistUserRequest request, SystemLocale systemLocale) {
    log.info(
        "Creating new waitlist user | email={}, language={}",
        request.email(),
        systemLocale.getLanguage());
    WaitlistUser waitlistUser =
        this.waitlistUserRepository.save(new WaitlistUser().create(request, systemLocale.getId()));
    log.info("Waitlist user created successfully | email={}", waitlistUser.getEmail());
    return waitlistUser;
  }

  @Transactional
  public void confirmWaitlistUser(String token) {
    log.info("Confirming waitlist user | token={}", token);

    if (!tokenService.validateToken(token, TokenType.EMAIL_CONFIRMATION)) {
      log.error("Invalid token | token={}", token);
      throw new IllegalArgumentException("Invalid or expired token");
    }

    Map<String, Object> metadata =
        tokenService.getMetadataFromToken(token, TokenType.EMAIL_CONFIRMATION);

    String email = tokenService.getEmailFromMetadata(metadata);
    log.info("Email extracted from token metadata | email={}", email);

    WaitlistUser waitlistUser =
        waitlistUserRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  log.error("Waitlist user not found | email={}", email);
                  return new EntityNotFoundException("Waitlist user not found");
                });

    if (!waitlistUser.isConfirmed()) {
      waitlistUser.confirm();
      waitlistUserRepository.save(waitlistUser);
      log.info("Waitlist user confirmed successfully | email={}", email);
    } else {
      log.info("Waitlist user already confirmed | email={}", email);
    }

    tokenService.invalidateToken(token);
    log.info("Token invalidated after confirmation | token={}", token);
  }

  private String getLocaleCode(String acceptLanguage) {
    if (acceptLanguage == null || acceptLanguage.isEmpty()) {
      log.warn(
          "No accept language provided, using default locale | acceptLanguage={}", acceptLanguage);
      return "pt-BR";
    }
    String normalized = acceptLanguage.toLowerCase().trim();
    if (normalized.startsWith("pt")) {
      log.info("Using Portuguese locale | acceptLanguage={}", acceptLanguage);
      return "pt-BR";
    }
    log.info("Using English locale | acceptLanguage={}", acceptLanguage);
    return "en-US";
  }
}
