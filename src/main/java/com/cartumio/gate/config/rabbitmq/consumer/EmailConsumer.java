package com.cartumio.gate.config.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.cartumio.gate.config.rabbitmq.RabbitMQConfig;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.exception.EmailFailedException;
import com.cartumio.gate.service.email.EmailService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmail(Email email) {
        log.info("Consuming email from queue | subject={}, to={}", 
                email.getSubject(),
                email.getTo() != null ? email.getTo().stream()
                        .map(EmailUser::getEmail)
                        .reduce((a, b) -> a + ", " + b).orElse("N/A") : "N/A");

        try {
            emailService.processEmail(email);
            log.info("Email processed successfully from queue | subject={}", email.getSubject());
        } catch (EmailFailedException e) {
            log.error("Email processing failed | subject={}, error={}", 
                    email.getSubject(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing email | subject={}, error={}", 
                    email.getSubject(), e.getMessage(), e);
            throw new EmailFailedException("Unexpected error processing email", e);
        }
    }
}
