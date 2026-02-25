package com.cartumio.gate.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.repository.SystemLocaleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SystemLocaleService - Tests")
class SystemLocaleServiceTest {

  private SystemLocaleService systemLocaleService;
  private SystemLocaleRepository systemLocaleRepository;
  private SystemLocale systemLocale;

  private static final String LOCALE_CODE = "pt-BR";
  private static final String LANGUAGE = "pt";
  private static final String COUNTRY = "BR";

  @BeforeEach
  void setUp() {
    systemLocaleRepository = mock(SystemLocaleRepository.class);
    systemLocaleService = new SystemLocaleService(systemLocaleRepository);

    systemLocale = new SystemLocale();
    systemLocale.setId(UUID.randomUUID());
    systemLocale.setCode(LOCALE_CODE);
    systemLocale.setLanguage(LANGUAGE);
    systemLocale.setCountry(COUNTRY);
    systemLocale.setActive(true);
  }

  @Test
  @DisplayName("Should find active system locale by code successfully")
  void testFindActiveByCodeSuccessfully() {
    when(systemLocaleRepository.findActiveByCode(LOCALE_CODE))
        .thenReturn(Optional.of(systemLocale));

    SystemLocale result = systemLocaleService.findActiveByCode(LOCALE_CODE);

    verify(systemLocaleRepository).findActiveByCode(LOCALE_CODE);
    assert result != null : "System locale should not be null";
    assert result.getCode().equals(LOCALE_CODE) : "Code should match";
    assert result.getLanguage().equals(LANGUAGE) : "Language should match";
    assert result.getCountry().equals(COUNTRY) : "Country should match";
    assert result.isActive() : "Should be active";
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when system locale not found")
  void testFindActiveByCodeNotFound() {
    when(systemLocaleRepository.findActiveByCode(LOCALE_CODE)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class, () -> systemLocaleService.findActiveByCode(LOCALE_CODE));

    verify(systemLocaleRepository).findActiveByCode(LOCALE_CODE);
  }

  @Test
  @DisplayName(
      "Should throw EntityNotFoundException with correct message when system locale not found")
  void testFindActiveByCodeNotFoundMessage() {
    when(systemLocaleRepository.findActiveByCode(LOCALE_CODE)).thenReturn(Optional.empty());

    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class, () -> systemLocaleService.findActiveByCode(LOCALE_CODE));

    assert exception.getMessage().equals("System locale not found")
        : "Exception message should match";
    verify(systemLocaleRepository).findActiveByCode(LOCALE_CODE);
  }
}
