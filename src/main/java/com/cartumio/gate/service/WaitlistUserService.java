package com.cartumio.gate.service;

import org.springframework.stereotype.Service;

import com.cartumio.gate.domain.WaitlistUser;
import com.cartumio.gate.dto.WaitlistUserRequest;
import com.cartumio.gate.repository.WaitlistUserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class WaitlistUserService {
    
    private final WaitlistUserRepository waitlistUserRepository;

    @Transactional
    public void createWaitlistUser(WaitlistUserRequest request) {
        log.info("Creating new waitlist user: {}", request.email());
        if (waitlistUserRepository.existsByEmail(request.email())) {
            log.error("Waitlist user already exists: {}", request.email());
            throw new EntityExistsException("Waitlist user already exists");
        }

        WaitlistUser waitlistUser = new WaitlistUser().create(request);
        waitlistUserRepository.save(waitlistUser);
        log.info("Waitlist user created successfully");
    }

    @Transactional
    public void confirmWaitlistUser(String email) {
        log.info("Confirming waitlist user: {}", email);
        WaitlistUser waitlistUser = waitlistUserRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Waitlist user not found"));
        waitlistUser.confirm();
        waitlistUserRepository.save(waitlistUser);
        log.info("Waitlist user confirmed successfully");
    }
}
