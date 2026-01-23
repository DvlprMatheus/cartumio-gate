package com.cartumio.gate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cartumio.gate.domain.SystemLocale;

@Repository
public interface SystemLocaleRepository extends JpaRepository<SystemLocale, UUID> {

    @Query("SELECT sl FROM SystemLocale sl WHERE sl.code = :code AND sl.active = true")
    Optional<SystemLocale> findActiveByCode(String code);
}
