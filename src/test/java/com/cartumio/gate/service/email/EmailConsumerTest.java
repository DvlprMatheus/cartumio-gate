package com.cartumio.gate.service.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.exception.EmailFailedException;

import java.util.List;

@DisplayName("EmailConsumer - Tests")
class EmailConsumerTest {

    private EmailConsumer emailConsumer;
    private EmailService emailService;
    private Email email;

    private static final String SUBJECT = "Test Subject";
    private static final String BODY = "Test Body";
    private static final String FROM_NAME = "Sender";
    private static final String FROM_EMAIL = "sender@example.com";
    private static final String TO_NAME = "Receiver";
    private static final String TO_EMAIL = "receiver@example.com";

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        emailConsumer = new EmailConsumer(emailService);

        EmailUser fromUser = new EmailUser(FROM_NAME, FROM_EMAIL);
        EmailUser toUser = new EmailUser(TO_NAME, TO_EMAIL);

        email = new Email();
        email.setFrom(fromUser);
        email.setTo(List.of(toUser));
        email.setSubject(SUBJECT);
        email.setBody(BODY);
    }

    @Test
    @DisplayName("Should consume and process email successfully")
    void testConsumeEmailSuccessfully() {
        emailConsumer.consumeEmail(email);

        verify(emailService).processEmail(email);
    }

    @Test
    @DisplayName("Should throw EmailFailedException when EmailService fails")
    void testConsumeEmailThrowsExceptionWhenEmailServiceFails() {
        EmailFailedException exception = new EmailFailedException("Email sending failed");
        doThrow(exception).when(emailService).processEmail(any(Email.class));

        assertThrows(EmailFailedException.class, () -> emailConsumer.consumeEmail(email));

        verify(emailService).processEmail(email);
    }

    @Test
    @DisplayName("Should wrap unexpected exception in EmailFailedException")
    void testConsumeEmailWrapsUnexpectedException() {
        RuntimeException exception = new RuntimeException("Unexpected error");
        doThrow(exception).when(emailService).processEmail(any(Email.class));

        assertThrows(EmailFailedException.class, () -> emailConsumer.consumeEmail(email));

        verify(emailService).processEmail(email);
    }

    @Test
    @DisplayName("Should handle email with null to list")
    void testConsumeEmailWithNullToList() {
        email.setTo(null);

        emailConsumer.consumeEmail(email);

        verify(emailService).processEmail(email);
    }
}
