package com.cartumio.gate.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.config.ratelimit.RateLimitFilter;
import com.cartumio.gate.config.ratelimit.RateLimitProperties;
import com.cartumio.gate.config.ratelimit.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RateLimitFilter - Tests")
class RateLimitFilterTest {

  private RateLimitFilter rateLimitFilter;
  private RateLimitService rateLimitService;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() throws IOException {
    RateLimitProperties properties = new RateLimitProperties();

    RateLimitProperties.Rule defaultRule = new RateLimitProperties.Rule();
    defaultRule.setPath("default");
    defaultRule.setCapacity(5);
    defaultRule.setRefill(Duration.ofMinutes(1));
    properties.setDefaultRule(defaultRule);

    List<RateLimitProperties.Rule> rules = new ArrayList<>();
    properties.setRules(rules);

    rateLimitService = new RateLimitService(properties);
    rateLimitFilter = new RateLimitFilter(rateLimitService);

    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");
    when(request.getHeader("X-Fowarded-For")).thenReturn(null);

    response = mock(HttpServletResponse.class);
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    filterChain = mock(FilterChain.class);
  }

  @Test
  @DisplayName("Should allow request when bucket has tokens available")
  void testDoFilter_AllowsRequest() throws ServletException, IOException {
    rateLimitFilter.doFilter(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  @Test
  @DisplayName("Should block request when rate limit is exceeded")
  void testDoFilter_BlocksRequestWhenRateLimitExceeded() throws ServletException, IOException {
    String ip = "192.168.1.100";
    String path = "/api/test";

    Bucket bucket = rateLimitService.resolveBucket(ip, path);
    for (int i = 0; i < 5; i++) {
      bucket.tryConsume(1);
    }

    rateLimitFilter.doFilter(request, response, filterChain);

    verify(filterChain, never()).doFilter(any(), any());
    verify(response).setStatus(429);
    verify(response).setContentType("application/json");

    printWriter.flush();
    String responseBody = stringWriter.toString();
    assertTrue(responseBody.contains("Too Many Requests"));
    assertTrue(responseBody.contains("Rate limit exceeded"));
  }

  @Test
  @DisplayName("Should use X-Forwarded-For header when present")
  void testGetIp_WithXForwardedFor() throws ServletException, IOException {
    when(request.getHeader("X-Fowarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");

    rateLimitFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(request).getHeader("X-Fowarded-For");
  }

  @Test
  @DisplayName("Should use RemoteAddr when X-Forwarded-For is not present")
  void testGetIp_WithoutXForwardedFor() throws ServletException, IOException {
    when(request.getHeader("X-Fowarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");

    rateLimitFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(request).getRemoteAddr();
  }

  @Test
  @DisplayName("Should use first IP when X-Forwarded-For has multiple IPs")
  void testGetIp_MultipleXForwardedFor() throws ServletException, IOException {
    when(request.getHeader("X-Fowarded-For")).thenReturn("10.0.0.1, 172.16.0.1, 192.168.1.1");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");

    rateLimitFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should treat empty X-Forwarded-For as absent")
  void testGetIp_EmptyXForwardedFor() throws ServletException, IOException {
    when(request.getHeader("X-Fowarded-For")).thenReturn("");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");

    rateLimitFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(request).getRemoteAddr();
  }

  @Test
  @DisplayName("Should return correct JSON when rate limit is exceeded")
  void testRateLimitExceededResponse() throws ServletException, IOException {
    String ip = "192.168.1.200";
    String path = "/api/test";

    Bucket bucket = rateLimitService.resolveBucket(ip, path);
    for (int i = 0; i < 5; i++) {
      bucket.tryConsume(1);
    }

    when(request.getRemoteAddr()).thenReturn(ip);

    rateLimitFilter.doFilter(request, response, filterChain);

    printWriter.flush();
    String responseBody = stringWriter.toString();

    assertTrue(responseBody.contains("\"error\""));
    assertTrue(responseBody.contains("\"message\""));
    assertTrue(responseBody.contains("Too Many Requests"));
    assertTrue(responseBody.contains("Rate limit exceeded"));
  }
}
