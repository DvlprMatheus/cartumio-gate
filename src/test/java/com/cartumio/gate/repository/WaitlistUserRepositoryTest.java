package com.cartumio.gate.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.domain.WaitlistUser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WaitlistUserRepository - Tests")
class WaitlistUserRepositoryTest {

  private WaitlistUserRepository waitlistUserRepository;
  private WaitlistUser waitlistUser;

  @BeforeEach
  void setUp() {
    waitlistUserRepository = mock(WaitlistUserRepository.class);
    waitlistUser = mock(WaitlistUser.class);
  }

  @Test
  @DisplayName("Should save waitlist user successfully")
  void testSaveWaitlistUserSuccessfully() {
    when(waitlistUserRepository.save(waitlistUser)).thenReturn(waitlistUser);
    waitlistUserRepository.save(waitlistUser);
    verify(waitlistUserRepository).save(waitlistUser);
  }

  @Test
  @DisplayName("Should find waitlist user by email successfully")
  void testFindByEmailSuccessfully() {
    when(waitlistUserRepository.findByEmail(waitlistUser.getEmail()))
        .thenReturn(Optional.of(waitlistUser));
    waitlistUserRepository.findByEmail(waitlistUser.getEmail());
    verify(waitlistUserRepository).findByEmail(waitlistUser.getEmail());
  }

  @Test
  @DisplayName("Should return empty when waitlist user not found")
  void testFindByEmailNotFound() {
    when(waitlistUserRepository.findByEmail(waitlistUser.getEmail())).thenReturn(Optional.empty());
    waitlistUserRepository.findByEmail(waitlistUser.getEmail());
    verify(waitlistUserRepository).findByEmail(waitlistUser.getEmail());
  }
}
