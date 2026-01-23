package com.cartumio.gate.service;

import org.springframework.stereotype.Service;

import com.cartumio.gate.domain.SystemLocale;
import com.cartumio.gate.repository.SystemLocaleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class SystemLocaleService {
    
    private final SystemLocaleRepository systemLocaleRepository;

    public SystemLocale findActiveByCode(String code) {
        log.info("Finding active system locale by code | code={}", code);
        return systemLocaleRepository.findActiveByCode(code)
            .orElseThrow(() -> {
                log.error("System locale not found | code={}", code);
                return new EntityNotFoundException("System locale not found");
            });
    }
}
