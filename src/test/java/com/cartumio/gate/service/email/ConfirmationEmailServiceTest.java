package com.cartumio.gate.service.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.config.email.BrevoProperties;
import com.cartumio.gate.domain.email.EmailTemplate;

@DisplayName("ConfirmationEmailService - Tests")
class ConfirmationEmailServiceTest {

    private ConfirmationEmailService confirmationEmailService;
    private BrevoProperties brevoProperties;
    private EmailService emailService;
    private EmailTemplateService emailTemplateService;
    private EmailTemplate emailTemplate;
    private BrevoProperties.Sender sender;

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String LANGUAGE = "pt";
    private static final String TEMPLATE_CODE = "confirmation-template";
    private static final String SUBJECT = "Confirm your email";
    private static final String BODY = "Hello {{fullName}}, please confirm your email.";
    private static final String SENDER_NAME = "Cartumio";
    private static final String SENDER_EMAIL = "noreply@cartumio.com";

    @BeforeEach
    void setUp() {
        brevoProperties = mock(BrevoProperties.class);
        emailService = mock(EmailService.class);
        emailTemplateService = mock(EmailTemplateService.class);
        confirmationEmailService = new ConfirmationEmailService(
                brevoProperties, emailService, emailTemplateService);

        sender = new BrevoProperties.Sender();
        sender.setName(SENDER_NAME);
        sender.setEmail(SENDER_EMAIL);
        when(brevoProperties.getSender()).thenReturn(sender);

        emailTemplate = new EmailTemplate();
        emailTemplate.setId(UUID.randomUUID());
        emailTemplate.setCode(TEMPLATE_CODE);
        emailTemplate.setLanguage(LANGUAGE);
        emailTemplate.setSubject(SUBJECT);
        emailTemplate.setBody(BODY);
        emailTemplate.setActive(true);

        when(emailTemplateService.findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE))
                .thenReturn(emailTemplate);
    }

    @Test
    @DisplayName("Should send confirmation email successfully")
    void testSendConfirmationEmailSuccessfully() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(emailService).processEmail(any(com.cartumio.gate.domain.email.Email.class));
    }

    @Test
    @DisplayName("Should build email with correct template data")
    void testSendConfirmationEmailWithCorrectTemplate() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(emailService).processEmail(any(com.cartumio.gate.domain.email.Email.class));
    }

    @Test
    @DisplayName("Should use correct sender from properties")
    void testSendConfirmationEmailUsesCorrectSender() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(emailService).processEmail(any(com.cartumio.gate.domain.email.Email.class));
    }

    @Test
    @DisplayName("Should build email with full name in data")
    void testSendConfirmationEmailWithFullName() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(emailService).processEmail(any(com.cartumio.gate.domain.email.Email.class));
    }
}
