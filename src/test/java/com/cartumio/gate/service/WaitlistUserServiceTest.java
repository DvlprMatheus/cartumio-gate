package com.cartumio.gate.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;
import com.cartumio.gate.service.email.ConfirmationEmailService;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@DisplayName("WaitlistUserService - Tests")
class WaitlistUserServiceTest {

    private WaitlistUserService waitlistUserService;
    private WaitlistUserRepository waitlistUserRepository;
    private ConfirmationEmailService confirmationEmailService;
    private SystemLocaleService systemLocaleService;
    private WaitlistUserRequest request;
    private SystemLocale systemLocale;

    private static final String LOCALE_CODE = "pt-BR";
    private static final String EMAIL = "john.doe@example.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LANGUAGE = "pt";

    @BeforeEach
    void setUp() {
        waitlistUserRepository = mock(WaitlistUserRepository.class);
        confirmationEmailService = mock(ConfirmationEmailService.class);
        systemLocaleService = mock(SystemLocaleService.class);
        waitlistUserService = new WaitlistUserService(
                waitlistUserRepository,
                confirmationEmailService,
                systemLocaleService);

        request = mock(WaitlistUserRequest.class);
        when(request.email()).thenReturn(EMAIL);
        when(request.firstName()).thenReturn(FIRST_NAME);
        when(request.lastName()).thenReturn(LAST_NAME);

        systemLocale = new SystemLocale();
        systemLocale.setId(UUID.randomUUID());
        systemLocale.setCode(LOCALE_CODE);
        systemLocale.setLanguage(LANGUAGE);
        systemLocale.setCountry("BR");
        systemLocale.setActive(true);
    }

    @Test
    @DisplayName("Should create waitlist user successfully and send confirmation email")
    void testCreateWaitlistUserSuccessfully() {
        when(waitlistUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(systemLocaleService.findActiveByCode(LOCALE_CODE)).thenReturn(systemLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.createWaitlistUser(request, LOCALE_CODE);

        verify(waitlistUserRepository).existsByEmail(EMAIL);
        verify(systemLocaleService).findActiveByCode(LOCALE_CODE);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(EMAIL),
                eq(LOCALE_CODE));
    }

    @Test
    @DisplayName("Should throw EntityExistsException and not send email when waitlist user already exists")
    void testCreateWaitlistUserAlreadyExists() {
        when(waitlistUserRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(EntityExistsException.class,
                () -> waitlistUserService.createWaitlistUser(request, LOCALE_CODE));

        verify(waitlistUserRepository).existsByEmail(EMAIL);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(systemLocaleService, never()).findActiveByCode(anyString());
        verify(confirmationEmailService, never()).sendConfirmationEmail(
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should confirm waitlist user successfully")
    void testConfirmWaitlistUserSuccessfully() {
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setEmail(EMAIL);
        waitlistUser.setConfirmed(false);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(waitlistUser));
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.confirmWaitlistUser(EMAIL);

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
    }

    @Test
    @DisplayName("Should set isConfirmed to true when confirming waitlist user")
    void testConfirmWaitlistUserSetsConfirmedToTrue() {
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setEmail(EMAIL);
        waitlistUser.setFirstName(FIRST_NAME);
        waitlistUser.setLastName(LAST_NAME);
        waitlistUser.setConfirmed(false);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(waitlistUser));
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.confirmWaitlistUser(EMAIL);

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        assert waitlistUser.isConfirmed() : "Waitlist user should be confirmed";
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when waitlist user not found for confirmation")
    void testConfirmWaitlistUserNotFound() {
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> waitlistUserService.confirmWaitlistUser(EMAIL));

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
    }

    @Test
    @DisplayName("Should create waitlist user with correct system locale id")
    void testCreateWaitlistUserWithSystemLocaleId() {
        UUID localeId = UUID.randomUUID();
        systemLocale.setId(localeId);
        when(waitlistUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(systemLocaleService.findActiveByCode(LOCALE_CODE)).thenReturn(systemLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            assert user.getSystemLocaleId().equals(localeId) : "System locale ID should match";
            assert user.getEmail().equals(EMAIL) : "Email should match";
            assert user.getFirstName().equals(FIRST_NAME) : "First name should match";
            assert user.getLastName().equals(LAST_NAME) : "Last name should match";
            assert !user.isConfirmed() : "User should not be confirmed initially";
            return user;
        });

        waitlistUserService.createWaitlistUser(request, LOCALE_CODE);

        verify(waitlistUserRepository).existsByEmail(EMAIL);
        verify(systemLocaleService).findActiveByCode(LOCALE_CODE);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(EMAIL),
                eq(LOCALE_CODE));
    }
}
