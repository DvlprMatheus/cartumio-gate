package com.cartumio.gate.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cartumio.gate.dto.request.WaitlistUserConfirmationRequest;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.service.WaitlistUserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@DisplayName("WaitlistUserController - Tests")
class WaitlistUserControllerTest {

    private WaitlistUserController waitlistUserController;
    private WaitlistUserService waitlistUserService;
    private HttpServletRequest servletRequest;
    private WaitlistUserRequest request;

    private static final String EMAIL = "john.doe@example.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ACCEPT_LANGUAGE = "pt-BR";
    private static final String TOKEN = "test-token-123";

    @BeforeEach
    void setUp() {
        waitlistUserService = mock(WaitlistUserService.class);
        waitlistUserController = new WaitlistUserController(waitlistUserService);
        servletRequest = mock(HttpServletRequest.class);
        request = new WaitlistUserRequest(FIRST_NAME, LAST_NAME, EMAIL);
    }

    @Test
    @DisplayName("Should create waitlist user successfully and return 201 CREATED")
    void testCreateWaitlistUserSuccessfully() {
        when(servletRequest.getHeader("Accept-Language")).thenReturn(ACCEPT_LANGUAGE);

        ResponseEntity<Void> response = waitlistUserController.createWaitlistUser(request, servletRequest);

        verify(waitlistUserService).createWaitlistUser(any(WaitlistUserRequest.class), eq(ACCEPT_LANGUAGE));
        assert response.getStatusCode() == HttpStatus.CREATED : "Status should be CREATED";
    }

    @Test
    @DisplayName("Should handle null Accept-Language header")
    void testCreateWaitlistUserWithNullAcceptLanguage() {
        when(servletRequest.getHeader("Accept-Language")).thenReturn(null);

        ResponseEntity<Void> response = waitlistUserController.createWaitlistUser(request, servletRequest);

        verify(waitlistUserService).createWaitlistUser(any(WaitlistUserRequest.class), eq(null));
        assert response.getStatusCode() == HttpStatus.CREATED : "Status should be CREATED";
    }

    @Test
    @DisplayName("Should call service with correct request and locale")
    void testCreateWaitlistUserCallsServiceWithCorrectParameters() {
        when(servletRequest.getHeader("Accept-Language")).thenReturn(ACCEPT_LANGUAGE);

        waitlistUserController.createWaitlistUser(request, servletRequest);

        verify(waitlistUserService).createWaitlistUser(request, ACCEPT_LANGUAGE);
    }

    @Test
    @DisplayName("Should confirm waitlist user successfully and return 200 OK")
    void testConfirmWaitlistUserSuccessfully() {
        WaitlistUserConfirmationRequest confirmationRequest = new WaitlistUserConfirmationRequest(TOKEN);

        ResponseEntity<Void> response = waitlistUserController.confirmWaitlistUser(confirmationRequest);

        verify(waitlistUserService).confirmWaitlistUser(TOKEN);
        assert response.getStatusCode() == HttpStatus.OK : "Status should be OK";
    }

    @Test
    @DisplayName("Should return 400 Bad Request when token is invalid")
    void testConfirmWaitlistUserInvalidToken() {
        WaitlistUserConfirmationRequest confirmationRequest = new WaitlistUserConfirmationRequest(TOKEN);
        doThrow(new IllegalArgumentException("Invalid or expired token"))
                .when(waitlistUserService).confirmWaitlistUser(TOKEN);

        ResponseEntity<Void> response = waitlistUserController.confirmWaitlistUser(confirmationRequest);

        verify(waitlistUserService).confirmWaitlistUser(TOKEN);
        assert response.getStatusCode() == HttpStatus.BAD_REQUEST : "Status should be BAD_REQUEST";
    }

    @Test
    @DisplayName("Should return 404 Not Found when waitlist user not found")
    void testConfirmWaitlistUserNotFound() {
        WaitlistUserConfirmationRequest confirmationRequest = new WaitlistUserConfirmationRequest(TOKEN);
        doThrow(new EntityNotFoundException("Waitlist user not found"))
                .when(waitlistUserService).confirmWaitlistUser(TOKEN);

        ResponseEntity<Void> response = waitlistUserController.confirmWaitlistUser(confirmationRequest);

        verify(waitlistUserService).confirmWaitlistUser(TOKEN);
        assert response.getStatusCode() == HttpStatus.NOT_FOUND : "Status should be NOT_FOUND";
    }
}
