package com.cartumio.gate.domain;

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
@Table(name = "system_locales")
public class SystemLocale extends AbstractEntity {
    
    @Column(name = "code", nullable = false, unique = true, length = 5)
    private String code;

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "active", nullable = false)
    private boolean active;
}
