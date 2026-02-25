package com.cartumio.gate.domain.email;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Email implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private List<EmailUser> to = new ArrayList<>();

  private EmailUser from;

  private String subject;

  private String body;

  private Map<String, Object> data = new HashMap<>();

  public Email addTo(List<EmailUser> to) {
    if (this.to == null || this.to.isEmpty()) {
      this.to = new ArrayList<>();
    }
    this.to.addAll(to);
    return this;
  }

  public Email addFrom(EmailUser from) {
    this.from = from;
    return this;
  }

  public Email addSubject(String subject) {
    this.subject = subject;
    return this;
  }

  public Email addBody(String body) {
    this.body = body;
    return this;
  }

  public Email addData(String key, Object value) {
    this.data.put(key, value);
    return this;
  }
}
