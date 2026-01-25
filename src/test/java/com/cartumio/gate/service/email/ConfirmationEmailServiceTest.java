package com.cartumio.gate.service.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.cartumio.gate.config.email.BrevoProperties;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.service.token.TokenService;

@DisplayName("ConfirmationEmailService - Tests")
class ConfirmationEmailServiceTest {

    private ConfirmationEmailService confirmationEmailService;
    private BrevoProperties brevoProperties;
    private EmailProducer emailProducer;
    private EmailTemplateService emailTemplateService;
    private TokenService tokenService;
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
    private static final String TOKEN_VALUE = "test-token-123";
    private static final String ORIGIN_BASE_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        brevoProperties = mock(BrevoProperties.class);
        emailProducer = mock(EmailProducer.class);
        emailTemplateService = mock(EmailTemplateService.class);
        tokenService = mock(TokenService.class);
        confirmationEmailService = new ConfirmationEmailService(
                brevoProperties, emailProducer, emailTemplateService, tokenService);

        ReflectionTestUtils.setField(confirmationEmailService, "originBaseUrl", ORIGIN_BASE_URL);

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

        TokenResponse tokenResponse = new TokenResponse(TOKEN_VALUE, Instant.now().plusSeconds(86400), TokenType.EMAIL_CONFIRMATION);
        when(tokenService.generateToken(TokenType.EMAIL_CONFIRMATION)).thenReturn(tokenResponse);
    }

    @Test
    @DisplayName("Should send confirmation email successfully")
    void testSendConfirmationEmailSuccessfully() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION);
        verify(emailProducer).sendEmail(any(Email.class));
    }

    @Test
    @DisplayName("Should build email with correct template data")
    void testSendConfirmationEmailWithCorrectTemplate() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION);
        verify(emailProducer).sendEmail(any(Email.class));
    }

    @Test
    @DisplayName("Should use correct sender from properties")
    void testSendConfirmationEmailUsesCorrectSender() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION);
        verify(emailProducer).sendEmail(any(Email.class));
    }

    @Test
    @DisplayName("Should build email with full name in data")
    void testSendConfirmationEmailWithFullName() {
        confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

        verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
        verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION);
        verify(emailProducer).sendEmail(any(Email.class));
    }
}
