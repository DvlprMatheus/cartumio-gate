package com.cartumio.gate.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.domain.SystemLocale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SystemLocaleRepository - Tests")
class SystemLocaleRepositoryTest {

  private SystemLocaleRepository systemLocaleRepository;
  private SystemLocale systemLocale;
  private String code;

  @BeforeEach
  void setUp() {
    systemLocaleRepository = mock(SystemLocaleRepository.class);
    systemLocale = mock(SystemLocale.class);
    code = "pt-BR";
  }

  @Test
  @DisplayName("Should save system locale successfully")
  void testSaveSystemLocaleSuccessfully() {
    when(systemLocaleRepository.save(systemLocale)).thenReturn(systemLocale);
    systemLocaleRepository.save(systemLocale);
    verify(systemLocaleRepository).save(systemLocale);
  }

  @Test
  @DisplayName("Should find active system locale by code successfully")
  void testFindActiveByCodeSuccessfully() {
    when(systemLocaleRepository.findActiveByCode(code)).thenReturn(Optional.of(systemLocale));
    systemLocaleRepository.findActiveByCode(code);
    verify(systemLocaleRepository).findActiveByCode(code);
  }

  @Test
  @DisplayName("Should return empty when active system locale not found")
  void testFindActiveByCodeNotFound() {
    when(systemLocaleRepository.findActiveByCode(code)).thenReturn(Optional.empty());
    systemLocaleRepository.findActiveByCode(code);
    verify(systemLocaleRepository).findActiveByCode(code);
  }
}
