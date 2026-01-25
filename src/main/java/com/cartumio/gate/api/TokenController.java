package com.cartumio.gate.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cartumio.gate.dto.request.token.TokenInvalidationRequest;
import com.cartumio.gate.dto.request.token.TokenVerificationRequest;
import com.cartumio.gate.dto.response.token.TokenVerificationResponse;
import com.cartumio.gate.service.token.TokenService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/gate/v1/tokens")
@AllArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/verify")
    public ResponseEntity<TokenVerificationResponse> verifyToken(
            @Valid @RequestBody TokenVerificationRequest request) {
        log.info("Token verification requested | tokenType={}", request.tokenType());

        TokenVerificationResponse response = tokenService.validateTokenWithDetails(
                request.token(), request.tokenType());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invalidate")
    public ResponseEntity<Void> invalidateToken(
            @Valid @RequestBody TokenInvalidationRequest request) {
        log.info("Token invalidation requested");

        boolean invalidated = tokenService.invalidateToken(request.token());

        if (invalidated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
