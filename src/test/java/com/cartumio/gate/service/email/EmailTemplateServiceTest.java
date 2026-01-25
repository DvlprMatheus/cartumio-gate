package com.cartumio.gate.service.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.repository.EmailTemplateRepository;

import jakarta.persistence.EntityNotFoundException;

@DisplayName("EmailTemplateService - Tests")
class EmailTemplateServiceTest {

    private EmailTemplateService emailTemplateService;
    private EmailTemplateRepository emailTemplateRepository;
    private EmailTemplate emailTemplate;

    private static final String TEMPLATE_CODE = "confirmation-template";
    private static final String LANGUAGE = "pt";
    private static final String SUBJECT = "Confirm your email";
    private static final String BODY = "Hello {{fullName}}, please confirm your email.";

    @BeforeEach
    void setUp() {
        emailTemplateRepository = mock(EmailTemplateRepository.class);
        emailTemplateService = new EmailTemplateService(emailTemplateRepository);

        emailTemplate = new EmailTemplate();
        emailTemplate.setId(UUID.randomUUID());
        emailTemplate.setCode(TEMPLATE_CODE);
        emailTemplate.setLanguage(LANGUAGE);
        emailTemplate.setSubject(SUBJECT);
        emailTemplate.setBody(BODY);
        emailTemplate.setActive(true);
    }

    @Test
    @DisplayName("Should find active email template by code and language successfully")
    void testFindActiveByCodeAndLanguageSuccessfully() {
        when(emailTemplateRepository.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE))
                .thenReturn(Optional.of(emailTemplate));

        EmailTemplate result = emailTemplateService.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);

        verify(emailTemplateRepository).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        assert result != null : "Email template should not be null";
        assert result.getCode().equals(TEMPLATE_CODE) : "Code should match";
        assert result.getLanguage().equals(LANGUAGE) : "Language should match";
        assert result.getSubject().equals(SUBJECT) : "Subject should match";
        assert result.getBody().equals(BODY) : "Body should match";
        assert result.isActive() : "Should be active";
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when email template not found")
    void testFindActiveByCodeAndLanguageNotFound() {
        when(emailTemplateRepository.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> emailTemplateService.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE));

        verify(emailTemplateRepository).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException with correct message when email template not found")
    void testFindActiveByCodeAndLanguageNotFoundMessage() {
        when(emailTemplateRepository.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> emailTemplateService.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE));

        assert exception.getMessage().equals("Email template not found") : "Exception message should match";
        verify(emailTemplateRepository).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
    }
}
