package com.cartumio.gate.service.email;

import org.springframework.stereotype.Service;

import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.repository.EmailTemplateRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class EmailTemplateService {
    
    private final EmailTemplateRepository emailTemplateRepository;

    public EmailTemplate findActiveByCodeAndLanguage(String code, String language) {
        log.debug("Finding active email template | code={}, language={}", code, language);
        return emailTemplateRepository.findActiveByCodeAndLanguage(code, language)
            .orElseThrow(() -> {
                log.error("Email template not found | code={}, language={}", code, language);
                return new EntityNotFoundException("Email template not found");
            });
    }
}
