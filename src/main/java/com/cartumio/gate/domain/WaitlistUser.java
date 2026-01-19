package com.cartumio.gate.domain;

import java.time.Instant;
import java.util.UUID;

import com.cartumio.gate.dto.WaitlistUserRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "waitlist_users")
public class WaitlistUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "is_confirmed", nullable = false)
    private boolean isConfirmed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public WaitlistUser create(WaitlistUserRequest request) {
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setFirstName(request.firstName());
        waitlistUser.setLastName(request.lastName());
        waitlistUser.setEmail(request.email());
        waitlistUser.setConfirmed(false);
        return waitlistUser;
    }

    public void confirm() {
        this.isConfirmed = true;
    }
}
