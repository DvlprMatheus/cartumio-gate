package com.cartumio.gate.domain;

import java.util.UUID;

import com.cartumio.gate.dto.request.WaitlistUserRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class WaitlistUser extends AbstractEntity {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "is_confirmed", nullable = false)
    private boolean isConfirmed;

    @Column(name = "system_locale_id", nullable = false)
    private UUID systemLocaleId;

    public WaitlistUser create(WaitlistUserRequest request, UUID systemLocaleId) {
        WaitlistUser waitlistUser = new WaitlistUser();
        waitlistUser.setFirstName(request.firstName());
        waitlistUser.setLastName(request.lastName());
        waitlistUser.setEmail(request.email());
        waitlistUser.setConfirmed(false);
        waitlistUser.setSystemLocaleId(systemLocaleId);
        return waitlistUser;
    }

    public void confirm() {
        this.isConfirmed = true;
    }
}
