package com.cartumio.gate.domain;

import java.util.UUID;

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
@Table(name = "system_translates")
public class SystemTranslate extends AbstractEntity {

    @Column(name = "key", nullable = false, length = 50)
    private String key;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "system_locale_id", nullable = false)
    private UUID systemLocaleId;
}
