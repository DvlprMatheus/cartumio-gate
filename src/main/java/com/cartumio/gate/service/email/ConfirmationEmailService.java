package com.cartumio.gate.service.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cartumio.gate.config.email.BrevoProperties;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.service.token.TokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfirmationEmailService {

    private final BrevoProperties properties;
    private final EmailProducer emailProducer;
    private final EmailTemplateService emailTemplateService;
    private final TokenService tokenService;

    @Value("${origin.base-url:http://localhost:3000}")
    private String originBaseUrl;

    public ConfirmationEmailService(BrevoProperties properties, EmailProducer emailProducer,
            EmailTemplateService emailTemplateService, TokenService tokenService) {
        this.properties = properties;
        this.emailProducer = emailProducer;
        this.emailTemplateService = emailTemplateService;
        this.tokenService = tokenService;
    }

    public void sendConfirmationEmail(String firstName, String lastName, String email, String language) {
        String fullName = firstName + " " + lastName;
        log.info("Sending confirmation email | fullName={}, email={}, language={}", fullName, email, language);

        EmailTemplate emailTemplate = emailTemplateService.findActiveByCodeAndLanguage("confirmation-template",
                language);

        log.debug("Email template found | code={}, language={}", emailTemplate.getCode(), language);

        TokenResponse token = tokenService.generateToken(TokenType.EMAIL_CONFIRMATION);
        String confirmationUrl = originBaseUrl + "/confirm-email?token=" + token.token();

        log.debug("Token generated for confirmation email | tokenType={}, expiresAt={}",
                token.tokenType(), token.expiresAt());

        Email request = new Email()
                .addTo(List.of(new EmailUser(fullName, email)))
                .addFrom(new EmailUser(properties.getSender().getName(), properties.getSender().getEmail()))
                .addSubject(emailTemplate.getSubject())
                .addBody(emailTemplate.getBody())
                .addData("fullName", fullName)
                .addData("url", confirmationUrl);

        log.debug("Confirmation email request built | subject={}, to={}", emailTemplate.getSubject(), email);
        emailProducer.sendEmail(request);
        log.info("Confirmation email sent to queue successfully | email={}", email);
    }
}
