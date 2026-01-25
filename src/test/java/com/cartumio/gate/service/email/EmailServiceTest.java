package com.cartumio.gate.service.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.dto.response.EmailResponse;
import com.cartumio.gate.exception.EmailFailedException;
import reactor.core.publisher.Mono;

@DisplayName("EmailService - Tests")
class EmailServiceTest {

    private EmailService emailService;
    private WebClient brevoClient;
    private RequestBodyUriSpec requestBodyUriSpec;
    private RequestBodySpec requestBodySpec;
    private ResponseSpec responseSpec;
    private Email email;
    private EmailUser fromUser;
    private EmailUser toUser;

    private static final String SUBJECT = "Test Subject";
    private static final String BODY = "Test Body";
    private static final String FROM_NAME = "Sender";
    private static final String FROM_EMAIL = "sender@example.com";
    private static final String TO_NAME = "Receiver";
    private static final String TO_EMAIL = "receiver@example.com";

    @BeforeEach
    void setUp() {
        brevoClient = mock(WebClient.class);
        requestBodyUriSpec = mock(RequestBodyUriSpec.class);
        requestBodySpec = mock(RequestBodySpec.class);
        responseSpec = mock(ResponseSpec.class);
        emailService = new EmailService(brevoClient);

        fromUser = new EmailUser(FROM_NAME, FROM_EMAIL);
        toUser = new EmailUser(TO_NAME, TO_EMAIL);

        email = new Email();
        email.setFrom(fromUser);
        email.setTo(List.of(toUser));
        email.setSubject(SUBJECT);
        email.setBody(BODY);
    }

    @Test
    @DisplayName("Should process email successfully without template")
    void testProcessEmailSuccessfullyWithoutTemplate() {
        email.setData(null);
        when(brevoClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmailResponse.class)).thenReturn(Mono.just(new EmailResponse("123")));

        emailService.processEmail(email);

        verify(brevoClient).post();
        verify(requestBodyUriSpec).uri("/smtp/email");
        verify(requestBodySpec).bodyValue(any());
        verify(responseSpec).bodyToMono(EmailResponse.class);
    }

    @Test
    @DisplayName("Should process email with Mustache template successfully")
    void testProcessEmailWithTemplateSuccessfully() {
        Map<String, Object> data = new HashMap<>();
        data.put("fullName", "John Doe");
        email.setData(data);
        email.setBody("Hello {{fullName}}!");

        when(brevoClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmailResponse.class)).thenReturn(Mono.just(new EmailResponse("123")));

        emailService.processEmail(email);

        verify(brevoClient).post();
        verify(requestBodyUriSpec).uri("/smtp/email");
        verify(requestBodySpec).bodyValue(any());
        verify(responseSpec).bodyToMono(EmailResponse.class);
    }

    @Test
    @DisplayName("Should skip template processing when body is empty")
    void testProcessEmailSkipsTemplateWhenBodyEmpty() {
        email.setBody(null);
        email.setData(new HashMap<>());

        when(brevoClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmailResponse.class)).thenReturn(Mono.just(new EmailResponse("123")));

        emailService.processEmail(email);

        verify(brevoClient).post();
        verify(requestBodyUriSpec).uri("/smtp/email");
    }

    @Test
    @DisplayName("Should skip template processing when data is empty")
    void testProcessEmailSkipsTemplateWhenDataEmpty() {
        email.setData(null);

        when(brevoClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmailResponse.class)).thenReturn(Mono.just(new EmailResponse("123")));

        emailService.processEmail(email);

        verify(brevoClient).post();
        verify(requestBodyUriSpec).uri("/smtp/email");
    }

    @Test
    @DisplayName("Should throw EmailFailedException when template processing fails")
    void testProcessEmailThrowsExceptionWhenTemplateFails() {
        Map<String, Object> data = new HashMap<>();
        data.put("fullName", "John Doe");
        email.setData(data);
        email.setBody("Hello {{invalidTemplate!");

        assertThrows(EmailFailedException.class, () -> emailService.processEmail(email));

        verify(brevoClient, never()).post();
    }

    @Test
    @DisplayName("Should throw EmailFailedException when sending email fails")
    void testProcessEmailThrowsExceptionWhenSendingFails() {
        when(brevoClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmailResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        assertThrows(EmailFailedException.class, () -> emailService.processEmail(email));

        verify(brevoClient).post();
        verify(requestBodyUriSpec).uri("/smtp/email");
    }
}
