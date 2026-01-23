package com.cartumio.gate.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cartumio.gate.dto.request.WaitlistUserRequest;
import com.cartumio.gate.service.WaitlistUserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/gate/v1/waitlist-users")
@AllArgsConstructor
@Slf4j
public class WaitlistUserController {

    private final WaitlistUserService waitlistUserService;

    @PostMapping("/create")
    public ResponseEntity<Void> createWaitlistUser(@RequestBody WaitlistUserRequest request,
            HttpServletRequest servletRequest) {
        log.info("Create waitlist user called");
        String acceptLanguage = servletRequest.getHeader("Accept-Language");
        waitlistUserService.createWaitlistUser(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
