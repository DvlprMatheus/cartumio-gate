package com.cartumio.gate.service.email;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cartumio.gate.config.email.BrevoProperties;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailTemplate;
import com.cartumio.gate.domain.email.EmailUser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class ConfirmationEmailService {

    private final BrevoProperties properties;
    private final EmailService emailSenderService;
    private final EmailTemplateService emailTemplateService;

    public void sendConfirmationEmail(String firstName, String lastName, String email, String language) {
        String fullName = firstName + " " + lastName;
        log.info("Sending confirmation email | fullName={}, email={}, language={}", fullName, email, language);
        
        EmailTemplate emailTemplate = emailTemplateService.findActiveByCodeAndLanguage("confirmation-template",
                language);

        log.debug("Email template found | code={}, language={}", emailTemplate.getCode(), language);

        Email request = new Email()
            .addTo(List.of(new EmailUser(fullName, email)))
            .addFrom(new EmailUser(properties.getSender().getName(), properties.getSender().getEmail()))
            .addSubject(emailTemplate.getSubject())
            .addBody(emailTemplate.getBody())
            .addData("fullName", fullName)
            .addData("url", "http://localhost:3000/confirm-email?token=1234567890");

        log.debug("Confirmation email request built | subject={}, to={}", emailTemplate.getSubject(), email);
        emailSenderService.processEmail(request);
        log.info("Confirmation email sent successfully | email={}", email);
    }
}
