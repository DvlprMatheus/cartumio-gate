package com.cartumio.gate.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.domain.email.EmailTemplate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailTemplateRepository - Tests")
class EmailTemplateRepositoryTest {

  private EmailTemplateRepository emailTemplateRepository;
  private EmailTemplate emailTemplate;
  private String code;
  private String language;

  @BeforeEach
  void setUp() {
    emailTemplateRepository = mock(EmailTemplateRepository.class);
    emailTemplate = mock(EmailTemplate.class);
    code = "CONFIRMATION";
    language = "pt-BR";
  }

  @Test
  @DisplayName("Should save email template successfully")
  void testSaveEmailTemplateSuccessfully() {
    when(emailTemplateRepository.save(emailTemplate)).thenReturn(emailTemplate);
    emailTemplateRepository.save(emailTemplate);
    verify(emailTemplateRepository).save(emailTemplate);
  }

  @Test
  @DisplayName("Should find active email template by code and language successfully")
  void testFindActiveByCodeAndLanguageSuccessfully() {
    when(emailTemplateRepository.findActiveByCodeAndLanguage(code, language))
        .thenReturn(Optional.of(emailTemplate));
    emailTemplateRepository.findActiveByCodeAndLanguage(code, language);
    verify(emailTemplateRepository).findActiveByCodeAndLanguage(code, language);
  }

  @Test
  @DisplayName("Should return empty when active email template not found")
  void testFindActiveByCodeAndLanguageNotFound() {
    when(emailTemplateRepository.findActiveByCodeAndLanguage(code, language))
        .thenReturn(Optional.empty());
    emailTemplateRepository.findActiveByCodeAndLanguage(code, language);
    verify(emailTemplateRepository).findActiveByCodeAndLanguage(code, language);
  }
}
