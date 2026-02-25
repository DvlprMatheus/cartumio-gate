package com.cartumio.gate.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.config.email.BrevoProperties;
import com.cartumio.gate.config.rabbitmq.producer.EmailProducer;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.service.token.TokenService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

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
    confirmationEmailService =
        new ConfirmationEmailService(
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

    TokenResponse tokenResponse =
        new TokenResponse(
            TOKEN_VALUE, Instant.now().plusSeconds(86400), TokenType.EMAIL_CONFIRMATION);
    when(tokenService.generateToken(TokenType.EMAIL_CONFIRMATION, EMAIL)).thenReturn(tokenResponse);
  }

  @Test
  @DisplayName("Should send confirmation email successfully")
  void testSendConfirmationEmailSuccessfully() {
    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(emailTemplateService).findActiveByCodeAndLanguage(TEMPLATE_CODE, LANGUAGE);
    verify(tokenService).invalidateAllNonConsumedForEmail(TokenType.EMAIL_CONFIRMATION, EMAIL);
    verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION, EMAIL);
    verify(emailProducer).sendEmail(any(Email.class));
  }

  @Test
  @DisplayName("Should build email with correct recipient")
  void testSendConfirmationEmailWithCorrectRecipient() {
    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(emailProducer).sendEmail(emailCaptor.capture());
    Email capturedEmail = emailCaptor.getValue();

    assertNotNull(capturedEmail.getTo());
    assertEquals(1, capturedEmail.getTo().size());
    EmailUser recipient = capturedEmail.getTo().get(0);
    assertEquals(FIRST_NAME + " " + LAST_NAME, recipient.getName());
    assertEquals(EMAIL, recipient.getEmail());
  }

  @Test
  @DisplayName("Should use correct sender from properties")
  void testSendConfirmationEmailUsesCorrectSender() {
    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(emailProducer).sendEmail(emailCaptor.capture());
    Email capturedEmail = emailCaptor.getValue();

    assertNotNull(capturedEmail.getFrom());
    assertEquals(SENDER_NAME, capturedEmail.getFrom().getName());
    assertEquals(SENDER_EMAIL, capturedEmail.getFrom().getEmail());
  }

  @Test
  @DisplayName("Should build email with correct subject and body from template")
  void testSendConfirmationEmailWithCorrectTemplate() {
    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(emailProducer).sendEmail(emailCaptor.capture());
    Email capturedEmail = emailCaptor.getValue();

    assertEquals(SUBJECT, capturedEmail.getSubject());
    assertEquals(BODY, capturedEmail.getBody());
  }

  @Test
  @DisplayName("Should build email with full name and confirmation URL in data")
  void testSendConfirmationEmailWithFullNameAndUrl() {
    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(emailProducer).sendEmail(emailCaptor.capture());
    Email capturedEmail = emailCaptor.getValue();

    assertNotNull(capturedEmail.getData());
    String expectedFullName = FIRST_NAME + " " + LAST_NAME;
    assertEquals(expectedFullName, capturedEmail.getData().get("fullName"));
    String expectedUrl = ORIGIN_BASE_URL + "/confirm-email?token=" + TOKEN_VALUE;
    assertEquals(expectedUrl, capturedEmail.getData().get("url"));
  }

  @Test
  @DisplayName("Should pass email to token service when generating token")
  void testSendConfirmationEmailPassesEmailToTokenService() {
    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    verify(tokenService)
        .invalidateAllNonConsumedForEmail(eq(TokenType.EMAIL_CONFIRMATION), eq(EMAIL));
    verify(tokenService).generateToken(eq(TokenType.EMAIL_CONFIRMATION), eq(EMAIL));
  }

  @Test
  @DisplayName("Should invalidate previous tokens before generating new one")
  void testSendConfirmationEmailInvalidatesPreviousTokensBeforeGenerate() {
    confirmationEmailService.sendConfirmationEmail(FIRST_NAME, LAST_NAME, EMAIL, LANGUAGE);

    var inOrder = inOrder(tokenService);
    inOrder
        .verify(tokenService)
        .invalidateAllNonConsumedForEmail(TokenType.EMAIL_CONFIRMATION, EMAIL);
    inOrder.verify(tokenService).generateToken(TokenType.EMAIL_CONFIRMATION, EMAIL);
  }
}
