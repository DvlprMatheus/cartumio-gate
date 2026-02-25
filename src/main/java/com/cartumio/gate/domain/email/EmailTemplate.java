package com.cartumio.gate.domain.email;

import com.cartumio.gate.domain.AbstractEntity;
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
@Table(name = "email_templates")
public class EmailTemplate extends AbstractEntity {

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "language", nullable = false, length = 5)
  private String language;

  @Column(name = "subject", nullable = false, length = 255)
  private String subject;

  @Column(name = "body", nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "active", nullable = false)
  private boolean active;
}
