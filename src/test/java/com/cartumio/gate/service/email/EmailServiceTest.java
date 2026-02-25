package com.cartumio.gate.service.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.cartumio.gate.domain.email.Email;
import com.cartumio.gate.domain.email.EmailUser;
import com.cartumio.gate.dto.response.EmailResponse;
import com.cartumio.gate.exception.EmailFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("EmailService - Tests")
class EmailServiceTest {

  private EmailService emailService;
  private RestClient brevoRestClient;
  private MockRestServiceServer mockServer;
  private ObjectMapper objectMapper;
  private Email email;
  private EmailUser fromUser;
  private EmailUser toUser;

  private static final String SUBJECT = "Test Subject";
  private static final String BODY = "Test Body";
  private static final String FROM_NAME = "Sender";
  private static final String FROM_EMAIL = "sender@example.com";
  private static final String TO_NAME = "Receiver";
  private static final String TO_EMAIL = "receiver@example.com";
  private static final String BASE_URL = "https://api.brevo.com";

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(BASE_URL);
    mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    brevoRestClient = restClientBuilder.build();
    emailService = new EmailService(brevoRestClient);

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
  void testProcessEmailSuccessfullyWithoutTemplate() throws Exception {
    email.setData(null);

    EmailResponse response = new EmailResponse("123");
    String responseJson = objectMapper.writeValueAsString(response);

    mockServer
        .expect(requestTo(BASE_URL + "/smtp/email"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    emailService.processEmail(email);

    mockServer.verify();
  }

  @Test
  @DisplayName("Should process email with Mustache template successfully")
  void testProcessEmailWithTemplateSuccessfully() throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("fullName", "John Doe");
    email.setData(data);
    email.setBody("Hello {{fullName}}!");

    EmailResponse response = new EmailResponse("123");
    String responseJson = objectMapper.writeValueAsString(response);

    mockServer
        .expect(requestTo(BASE_URL + "/smtp/email"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    emailService.processEmail(email);

    mockServer.verify();
  }

  @Test
  @DisplayName("Should skip template processing when body is empty")
  void testProcessEmailSkipsTemplateWhenBodyEmpty() throws Exception {
    email.setBody(null);
    email.setData(new HashMap<>());

    EmailResponse response = new EmailResponse("123");
    String responseJson = objectMapper.writeValueAsString(response);

    mockServer
        .expect(requestTo(BASE_URL + "/smtp/email"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    emailService.processEmail(email);

    mockServer.verify();
  }

  @Test
  @DisplayName("Should skip template processing when data is empty")
  void testProcessEmailSkipsTemplateWhenDataEmpty() throws Exception {
    email.setData(null);

    EmailResponse response = new EmailResponse("123");
    String responseJson = objectMapper.writeValueAsString(response);

    mockServer
        .expect(requestTo(BASE_URL + "/smtp/email"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    emailService.processEmail(email);

    mockServer.verify();
  }

  @Test
  @DisplayName("Should throw EmailFailedException when template processing fails")
  void testProcessEmailThrowsExceptionWhenTemplateFails() {
    Map<String, Object> data = new HashMap<>();
    data.put("fullName", "John Doe");
    email.setData(data);
    email.setBody("Hello {{invalidTemplate!");

    assertThrows(EmailFailedException.class, () -> emailService.processEmail(email));

    mockServer.verify();
  }

  @Test
  @DisplayName("Should throw EmailFailedException when sending email fails")
  void testProcessEmailThrowsExceptionWhenSendingFails() {
    mockServer
        .expect(requestTo(BASE_URL + "/smtp/email"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andRespond(withServerError());

    assertThrows(EmailFailedException.class, () -> emailService.processEmail(email));

    mockServer.verify();
  }
}
