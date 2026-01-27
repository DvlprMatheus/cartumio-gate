package com.cartumio.gate.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cartumio.gate.dto.request.WaitlistUserConfirmationRequest;
import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.service.WaitlistUserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/gate/v1/waitlist-users")
@AllArgsConstructor
@Slf4j
public class WaitlistUserController {

    private final WaitlistUserService waitlistUserService;

    @PostMapping("/create-or-resend")
    public ResponseEntity<Void> createOrResendConfirmationEmail(@RequestBody WaitlistUserRequest request,
            HttpServletRequest servletRequest) {
        log.info("Create or resend confirmation email called");
        String acceptLanguage = servletRequest.getHeader("Accept-Language");
        waitlistUserService.createOrResendConfirmationEmail(request, acceptLanguage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmWaitlistUser(
            @Valid @RequestBody WaitlistUserConfirmationRequest request) {
        log.info("Confirm waitlist user called");
        try {
            waitlistUserService.confirmWaitlistUser(request.token());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid token | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            log.error("Waitlist user not found | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
