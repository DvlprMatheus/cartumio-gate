package com.cartumio.gate.config.rabbitmq.producer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.cartumio.gate.config.rabbitmq.RabbitMQConfig;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;

import java.util.List;

@DisplayName("EmailProducer - Tests")
class EmailProducerTest {

    private EmailProducer emailProducer;
    private RabbitTemplate rabbitTemplate;
    private Email email;

    private static final String SUBJECT = "Test Subject";
    private static final String BODY = "Test Body";
    private static final String FROM_NAME = "Sender";
    private static final String FROM_EMAIL = "sender@example.com";
    private static final String TO_NAME = "Receiver";
    private static final String TO_EMAIL = "receiver@example.com";

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        emailProducer = new EmailProducer(rabbitTemplate);

        EmailUser fromUser = new EmailUser(FROM_NAME, FROM_EMAIL);
        EmailUser toUser = new EmailUser(TO_NAME, TO_EMAIL);

        email = new Email();
        email.setFrom(fromUser);
        email.setTo(List.of(toUser));
        email.setSubject(SUBJECT);
        email.setBody(BODY);
    }

    @Test
    @DisplayName("Should send email to queue successfully")
    void testSendEmailToQueueSuccessfully() {
        emailProducer.sendEmail(email);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EMAIL_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                eq(email)
        );
    }

    @Test
    @DisplayName("Should throw RuntimeException when RabbitTemplate fails")
    void testSendEmailThrowsExceptionWhenRabbitTemplateFails() {
        doThrow(new RuntimeException("Connection error"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq(RabbitMQConfig.EMAIL_EXCHANGE),
                        eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                        eq(email)
                );

        assertThrows(RuntimeException.class, () -> emailProducer.sendEmail(email));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EMAIL_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                eq(email)
        );
    }

    @Test
    @DisplayName("Should send email with null to list")
    void testSendEmailWithNullToList() {
        email.setTo(null);

        emailProducer.sendEmail(email);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EMAIL_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                eq(email)
        );
    }
}
