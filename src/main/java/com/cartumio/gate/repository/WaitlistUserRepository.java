package com.cartumio.gate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cartumio.gate.domain.WaitlistUser;

@Repository
public interface WaitlistUserRepository extends JpaRepository<WaitlistUser, UUID> {

    Optional<WaitlistUser> findByEmail(String email);
}
