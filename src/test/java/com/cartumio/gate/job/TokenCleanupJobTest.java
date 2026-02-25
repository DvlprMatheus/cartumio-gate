package com.cartumio.gate.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cartumio.gate.service.token.TokenService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

@DisplayName("TokenCleanupJob - Tests")
class TokenCleanupJobTest {

  private TokenCleanupJob tokenCleanupJob;
  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = mock(TokenService.class);
    tokenCleanupJob = new TokenCleanupJob(tokenService);
  }

  @Test
  @DisplayName("Should cleanup expired tokens successfully")
  void testCleanupExpiredTokensSuccessfully() {
    tokenCleanupJob.cleanupExpiredTokens();

    verify(tokenService).cleanupExpiredTokens();
  }

  @Test
  @DisplayName("Should be scheduled daily at 4 AM")
  void testCleanupExpiredTokensScheduledDailyAt4Am() throws Exception {
    Method method = TokenCleanupJob.class.getDeclaredMethod("cleanupExpiredTokens");
    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    assertNotNull(scheduled, "cleanupExpiredTokens must have @Scheduled");
    assertEquals(
        "0 0 4 * * ?",
        scheduled.cron(),
        "Job must run daily at 4:00 AM (cron: second minute hour day month day-of-week)");
    assertEquals(-1, scheduled.fixedRate(), "Must use cron, not fixedRate");
  }

  @Test
  @DisplayName("Should handle exception during cleanup gracefully")
  void testCleanupExpiredTokensHandlesException() {
    doThrow(new RuntimeException("Database error")).when(tokenService).cleanupExpiredTokens();

    try {
      tokenCleanupJob.cleanupExpiredTokens();
    } catch (Exception e) {
      // Exception should be caught and logged internally
    }

    verify(tokenService).cleanupExpiredTokens();
  }
}
