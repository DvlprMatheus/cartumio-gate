package com.cartumio.gate.repository;

import com.cartumio.gate.domain.WaitlistUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitlistUserRepository extends JpaRepository<WaitlistUser, UUID> {

  Optional<WaitlistUser> findByEmail(String email);
}
