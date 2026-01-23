package com.cartumio.gate.service;

import org.springframework.stereotype.Service;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;
import com.cartumio.gate.service.email.ConfirmationEmailService;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class WaitlistUserService {

    private final WaitlistUserRepository waitlistUserRepository;
    private final ConfirmationEmailService confirmationEmailService;
    private final SystemLocaleService systemLocaleService;

    @Transactional
    public void createWaitlistUser(WaitlistUserRequest request, String acceptLanguage) {
        log.info("Creating new waitlist user | email={}, acceptLanguage={}", request.email(), acceptLanguage);
        if (waitlistUserRepository.existsByEmail(request.email())) {
            log.error("Waitlist user already exists | email={}", request.email());
            throw new EntityExistsException("Waitlist user already exists");
        }

        SystemLocale systemLocale = systemLocaleService.findActiveByCode(getLocaleCode(acceptLanguage));
        log.info("System locale found | code={}, language={}", systemLocale.getCode(), systemLocale.getLanguage());

        WaitlistUser waitlistUser = new WaitlistUser().create(request, systemLocale.getId());
        waitlistUserRepository.save(waitlistUser);
        log.info("Waitlist user created successfully | email={}", request.email());

        confirmationEmailService.sendConfirmationEmail(waitlistUser.getFirstName(), waitlistUser.getLastName(),
                waitlistUser.getEmail(), systemLocale.getCode());
    }

    @Transactional
    public void confirmWaitlistUser(String email) {
        log.info("Confirming waitlist user | email={}", email);
        WaitlistUser waitlistUser = waitlistUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Waitlist user not found | email={}", email);
                    return new EntityNotFoundException("Waitlist user not found");
                });
                
        waitlistUser.confirm();
        waitlistUserRepository.save(waitlistUser);
        log.info("Waitlist user confirmed successfully | email={}", email);
    }

    private String getLocaleCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            log.warn("No accept language provided, using default locale | acceptLanguage={}", acceptLanguage);
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
