package com.cartumio.gate.repository;

import com.cartumio.gate.domain.email.EmailTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

  @Query(
      """
            SELECT e FROM EmailTemplate e
            JOIN SystemLocale sl ON sl.code = e.language
            WHERE e.code = :code
                AND e.language = :language
                AND e.active = true
                AND sl.active = true
            """)
  Optional<EmailTemplate> findActiveByCodeAndLanguage(
      @Param("code") String code, @Param("language") String language);
}
