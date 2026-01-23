package com.cartumio.gate.service.email;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.dto.request.email.EmailRequest;
import com.cartumio.gate.dto.request.email.EmailUserRequest;
import com.cartumio.gate.dto.response.EmailResponse;
import com.cartumio.gate.exception.EmailFailedException;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    private final WebClient brevoClient;

    public EmailService(@Qualifier("brevoClient") WebClient brevoClient) {
        this.brevoClient = brevoClient;
    }

    public void processEmail(Email email) {
        log.info("Processing email | subject={}, to={}, from={}", 
                email.getSubject(), 
                email.getTo().stream().map(EmailUser::getEmail).collect(Collectors.joining(", ")),
                email.getFrom() != null ? email.getFrom().getEmail() : "N/A");
        processBody(email);
        EmailRequest emailRequest = buildEmailRequest(email);
        sendEmail(emailRequest);
        log.info("Email processed and sent successfully | subject={}", email.getSubject());
    }

    private void processBody(Email email) {
        if (email.getBody() == null || email.getBody().isEmpty()) {
            log.debug("Email body is empty, skipping template processing");
            return;
        }

        if (email.getData() == null || email.getData().isEmpty()) {
            log.debug("Email data is empty, skipping template processing");
            return;
        }

        log.debug("Processing email body with Mustache template | dataKeys={}", 
                email.getData().keySet());
        try {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(email.getBody()), "email-template");
            
            StringWriter writer = new StringWriter();
            mustache.execute(writer, email.getData());
            
            String processedBody = writer.toString();
            email.setBody(processedBody);
            log.debug("Email body processed successfully with Mustache template");
        } catch (Exception e) {
            log.error("Error processing email body with Mustache template | error={}", e.getMessage(), e);
            throw new EmailFailedException("Error processing email body with Mustache", e);
        }
    }

    private EmailRequest buildEmailRequest(Email email) {
        log.debug("Building email request | subject={}, recipientsCount={}", 
                email.getSubject(), 
                email.getTo().size());
        return new EmailRequest(
                email.getSubject(),
                new EmailUserRequest(email.getFrom()),
                email.getTo().stream()
                        .map(EmailUserRequest::new)
                        .collect(Collectors.toList()),
                email.getBody());
    }

    private void sendEmail(EmailRequest email) {
        log.info("Sending email via Brevo API | subject={}, to={}", 
                email.subject(),
                email.to().stream()
                        .map(EmailUserRequest::email)
                        .collect(Collectors.joining(", ")));
        try {
            brevoClient.post()
                    .uri("/smtp/email")
                    .bodyValue(email)
                    .retrieve()
                    .bodyToMono(EmailResponse.class)
                    .block();
            log.info("Email sent successfully via Brevo API | subject={}", email.subject());
        } catch (Exception e) {
            log.error("Error sending email via Brevo API | subject={}, error={}", 
                    email.subject(), e.getMessage(), e);
            throw new EmailFailedException(
                    "Error sending email", e);
        }
    }
}
