package com.cartumio.gate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;
import com.cartumio.gate.service.email.ConfirmationEmailService;
import com.cartumio.gate.service.token.TokenService;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@DisplayName("WaitlistUserService - Tests")
class WaitlistUserServiceTest {

    private WaitlistUserService waitlistUserService;
    private WaitlistUserRepository waitlistUserRepository;
    private ConfirmationEmailService confirmationEmailService;
    private SystemLocaleService systemLocaleService;
    private TokenService tokenService;
    private WaitlistUserRequest request;
    private SystemLocale systemLocale;

    private static final String LOCALE_CODE = "pt-BR";
    private static final String EMAIL = "john.doe@example.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LANGUAGE = "pt";
    private static final String TOKEN = "test-token-123";

    @BeforeEach
    void setUp() {
        waitlistUserRepository = mock(WaitlistUserRepository.class);
        confirmationEmailService = mock(ConfirmationEmailService.class);
        systemLocaleService = mock(SystemLocaleService.class);
        tokenService = mock(TokenService.class);
        waitlistUserService = new WaitlistUserService(
                waitlistUserRepository,
                confirmationEmailService,
                systemLocaleService,
                tokenService);

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
    @DisplayName("Should create waitlist user and send confirmation email when user does not exist")
    void testCreateOrResendCreatesUserAndSendsEmail() {
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode(LOCALE_CODE)).thenReturn(systemLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.createOrResendConfirmationEmail(request, LOCALE_CODE);

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(systemLocaleService).findActiveByCode(LOCALE_CODE);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(EMAIL),
                eq(LOCALE_CODE));
    }

    @Test
    @DisplayName("Should create waitlist user with correct system locale id when user does not exist")
    void testCreateOrResendCreatesUserWithCorrectLocaleId() {
        UUID localeId = UUID.randomUUID();
        systemLocale.setId(localeId);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode(LOCALE_CODE)).thenReturn(systemLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            assertEquals(localeId, user.getSystemLocaleId(), "System locale ID should match");
            assertEquals(EMAIL, user.getEmail(), "Email should match");
            assertEquals(FIRST_NAME, user.getFirstName(), "First name should match");
            assertEquals(LAST_NAME, user.getLastName(), "Last name should match");
            assertEquals(false, user.isConfirmed(), "User should not be confirmed initially");
            return user;
        });

        waitlistUserService.createOrResendConfirmationEmail(request, LOCALE_CODE);

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(systemLocaleService).findActiveByCode(LOCALE_CODE);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(EMAIL),
                eq(LOCALE_CODE));
    }

    @Test
    @DisplayName("Should resend confirmation email when user exists but is not confirmed")
    void testCreateOrResendResendsEmailWhenUserExistsAndNotConfirmed() {
        WaitlistUser existingUser = new WaitlistUser().create(request, systemLocale.getId());
        existingUser.setConfirmed(false);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(systemLocaleService.findActiveByCode(LOCALE_CODE)).thenReturn(systemLocale);

        waitlistUserService.createOrResendConfirmationEmail(request, LOCALE_CODE);

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(systemLocaleService).findActiveByCode(LOCALE_CODE);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(EMAIL),
                eq(LOCALE_CODE));
    }

    @Test
    @DisplayName("Should throw EntityExistsException when waitlist user already exists and is confirmed")
    void testCreateOrResendThrowsWhenUserExistsAndConfirmed() {
        WaitlistUser confirmedUser = new WaitlistUser().create(request, systemLocale.getId());
        confirmedUser.setConfirmed(true);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(confirmedUser));

        EntityExistsException exception = assertThrows(EntityExistsException.class,
                () -> waitlistUserService.createOrResendConfirmationEmail(request, LOCALE_CODE));

        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(systemLocaleService, never()).findActiveByCode(anyString());
        verify(confirmationEmailService, never()).sendConfirmationEmail(
                anyString(), anyString(), anyString(), anyString());
        assertEquals("Waitlist user already exists and is confirmed", exception.getMessage());
    }

    @Test
    @DisplayName("Should confirm waitlist user successfully")
    void testConfirmWaitlistUserSuccessfully() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", EMAIL);
        
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setEmail(EMAIL);
        waitlistUser.setConfirmed(false);
        
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(true);
        when(tokenService.getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(metadata);
        when(tokenService.getEmailFromMetadata(metadata)).thenReturn(EMAIL);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(waitlistUser));
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.confirmWaitlistUser(TOKEN);

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getEmailFromMetadata(metadata);
        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(tokenService).invalidateToken(TOKEN);
    }

    @Test
    @DisplayName("Should not confirm waitlist user if already confirmed")
    void testConfirmWaitlistUserAlreadyConfirmed() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", EMAIL);

        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setEmail(EMAIL);
        waitlistUser.setConfirmed(true);
        
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(true);
        when(tokenService.getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(metadata);
        when(tokenService.getEmailFromMetadata(metadata)).thenReturn(EMAIL);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(waitlistUser));

        waitlistUserService.confirmWaitlistUser(TOKEN);

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getEmailFromMetadata(metadata);
        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(tokenService).invalidateToken(TOKEN);
        assertEquals(true, waitlistUser.isConfirmed(), "Waitlist user already confirmed");
    }

    @Test
    @DisplayName("Should set isConfirmed to true when confirming waitlist user")
    void testConfirmWaitlistUserSetsConfirmedToTrue() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", EMAIL);
        
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setEmail(EMAIL);
        waitlistUser.setFirstName(FIRST_NAME);
        waitlistUser.setLastName(LAST_NAME);
        waitlistUser.setConfirmed(false);
        
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(true);
        when(tokenService.getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(metadata);
        when(tokenService.getEmailFromMetadata(metadata)).thenReturn(EMAIL);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(waitlistUser));
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> {
            WaitlistUser user = invocation.getArgument(0);
            return user;
        });

        waitlistUserService.confirmWaitlistUser(TOKEN);

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository).save(any(WaitlistUser.class));
        verify(tokenService).invalidateToken(TOKEN);
        assertEquals(true, waitlistUser.isConfirmed(), "Waitlist user should be confirmed");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token is invalid")
    void testConfirmWaitlistUserInvalidToken() {
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> waitlistUserService.confirmWaitlistUser(TOKEN));

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(waitlistUserRepository, never()).findByEmail(anyString());
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(tokenService, never()).invalidateToken(anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email not found in metadata")
    void testConfirmWaitlistUserEmailNotFoundInMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(true);
        when(tokenService.getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(metadata);
        when(tokenService.getEmailFromMetadata(metadata)).thenThrow(new IllegalArgumentException("Email not found in metadata"));

        assertThrows(IllegalArgumentException.class,
                () -> waitlistUserService.confirmWaitlistUser(TOKEN));

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getEmailFromMetadata(metadata);
        verify(waitlistUserRepository, never()).findByEmail(anyString());
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(tokenService, never()).invalidateToken(anyString());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when waitlist user not found for confirmation")
    void testConfirmWaitlistUserNotFound() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", EMAIL);
        
        when(tokenService.validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(true);
        when(tokenService.getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION)).thenReturn(metadata);
        when(tokenService.getEmailFromMetadata(metadata)).thenReturn(EMAIL);
        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> waitlistUserService.confirmWaitlistUser(TOKEN));

        verify(tokenService).validateToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getMetadataFromToken(TOKEN, TokenType.EMAIL_CONFIRMATION);
        verify(tokenService).getEmailFromMetadata(metadata);
        verify(waitlistUserRepository).findByEmail(EMAIL);
        verify(waitlistUserRepository, never()).save(any(WaitlistUser.class));
        verify(tokenService, never()).invalidateToken(anyString());
    }

    @Test
    @DisplayName("Should use pt-BR locale when acceptLanguage is null")
    void testGetLocaleCodeWithNullAcceptLanguage() {
        SystemLocale ptLocale = new SystemLocale();
        ptLocale.setId(UUID.randomUUID());
        ptLocale.setCode("pt-BR");
        ptLocale.setLanguage("pt");
        ptLocale.setCountry("BR");
        ptLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("pt-BR")).thenReturn(ptLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, null);

        verify(systemLocaleService).findActiveByCode("pt-BR");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("pt-BR"));
    }

    @Test
    @DisplayName("Should use pt-BR locale when acceptLanguage is empty")
    void testGetLocaleCodeWithEmptyAcceptLanguage() {
        SystemLocale ptLocale = new SystemLocale();
        ptLocale.setId(UUID.randomUUID());
        ptLocale.setCode("pt-BR");
        ptLocale.setLanguage("pt");
        ptLocale.setCountry("BR");
        ptLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("pt-BR")).thenReturn(ptLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, "");

        verify(systemLocaleService).findActiveByCode("pt-BR");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("pt-BR"));
    }

    @Test
    @DisplayName("Should use pt-BR locale when acceptLanguage starts with 'pt'")
    void testGetLocaleCodeWithPortugueseAcceptLanguage() {
        SystemLocale ptLocale = new SystemLocale();
        ptLocale.setId(UUID.randomUUID());
        ptLocale.setCode("pt-BR");
        ptLocale.setLanguage("pt");
        ptLocale.setCountry("BR");
        ptLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("pt-BR")).thenReturn(ptLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, "pt-BR");

        verify(systemLocaleService).findActiveByCode("pt-BR");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("pt-BR"));
    }

    @Test
    @DisplayName("Should use en-US locale when acceptLanguage does not start with 'pt'")
    void testGetLocaleCodeWithEnglishAcceptLanguage() {
        SystemLocale enLocale = new SystemLocale();
        enLocale.setId(UUID.randomUUID());
        enLocale.setCode("en-US");
        enLocale.setLanguage("en");
        enLocale.setCountry("US");
        enLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("en-US")).thenReturn(enLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, "en-US");

        verify(systemLocaleService).findActiveByCode("en-US");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("en-US"));
    }

    @Test
    @DisplayName("Should use en-US locale when acceptLanguage is 'en'")
    void testGetLocaleCodeWithEnAcceptLanguage() {
        SystemLocale enLocale = new SystemLocale();
        enLocale.setId(UUID.randomUUID());
        enLocale.setCode("en-US");
        enLocale.setLanguage("en");
        enLocale.setCountry("US");
        enLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("en-US")).thenReturn(enLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, "en");

        verify(systemLocaleService).findActiveByCode("en-US");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("en-US"));
    }

    @Test
    @DisplayName("Should normalize acceptLanguage to lowercase and trim whitespace")
    void testGetLocaleCodeNormalizesAcceptLanguage() {
        SystemLocale ptLocale = new SystemLocale();
        ptLocale.setId(UUID.randomUUID());
        ptLocale.setCode("pt-BR");
        ptLocale.setLanguage("pt");
        ptLocale.setCountry("BR");
        ptLocale.setActive(true);

        when(waitlistUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(systemLocaleService.findActiveByCode("pt-BR")).thenReturn(ptLocale);
        when(waitlistUserRepository.save(any(WaitlistUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        waitlistUserService.createOrResendConfirmationEmail(request, "  PT-BR  ");

        verify(systemLocaleService).findActiveByCode("pt-BR");
        verify(confirmationEmailService).sendConfirmationEmail(
                eq(FIRST_NAME), eq(LAST_NAME), eq(EMAIL), eq("pt-BR"));
    }
}
