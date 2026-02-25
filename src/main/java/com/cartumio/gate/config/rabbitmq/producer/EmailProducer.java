package com.cartumio.gate.config.rabbitmq.producer;

import com.cartumio.gate.config.rabbitmq.RabbitMQConfig;
import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class EmailProducer {

  private final RabbitTemplate rabbitTemplate;

  public void sendEmail(Email email) {
    log.info(
        "Sending email to queue | subject={}, to={}",
        email.getSubject(),
        email.getTo() != null
            ? email.getTo().stream()
                .map(EmailUser::getEmail)
                .reduce((a, b) -> a + ", " + b)
                .orElse("N/A")
            : "N/A");

    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, email);
      log.info("Email sent to queue successfully | subject={}", email.getSubject());
    } catch (Exception e) {
      log.error(
          "Error sending email to queue | subject={}, error={}",
          email.getSubject(),
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to send email to queue", e);
    }
  }
}
