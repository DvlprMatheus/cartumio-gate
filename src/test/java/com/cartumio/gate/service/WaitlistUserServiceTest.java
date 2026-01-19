package com.cartumio.gate.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.dto.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

@DisplayName("WaitlistUserService - Tests")
class WaitlistUserServiceTest {

    private WaitlistUserService waitlistUserService;
    private WaitlistUserRepository waitlistUserRepository;
    private WaitlistUserRequest request;

    @BeforeEach
    void setUp() {
        waitlistUserRepository = mock(WaitlistUserRepository.class);
        waitlistUserService = new WaitlistUserService(waitlistUserRepository);
        
        request = mock(WaitlistUserRequest.class);
        when(request.email()).thenReturn("john.doe@example.com");
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Doe");
    }

    @Test
    @DisplayName("Should create waitlist user successfully")
    void testCreateWaitlistUserSuccessfully() {
        when(waitlistUserRepository.existsByEmail(request.email())).thenReturn(false);
        waitlistUserService.createWaitlistUser(request);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
    }

    @Test
    @DisplayName("Should throw when waitlist user already exists")
    void testCreateWaitlistUserAlreadyExists() {
        when(waitlistUserRepository.existsByEmail(request.email())).thenReturn(true);
        assertThrows(EntityExistsException.class, () -> waitlistUserService.createWaitlistUser(request));
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
    }

    @Test
    @DisplayName("Should confirm waitlist user successfully")
    void testConfirmWaitlistUserSuccessfully() {
        when(waitlistUserRepository.findByEmail(request.email())).thenReturn(Optional.of(new WaitlistUser()));
        waitlistUserService.confirmWaitlistUser(request.email());
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
    }

    @Test
    @DisplayName("Should throw when waitlist user not found")
    void testConfirmWaitlistUserNotFound() {
        when(waitlistUserRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> waitlistUserService.confirmWaitlistUser(request.email()));
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
    }
}
