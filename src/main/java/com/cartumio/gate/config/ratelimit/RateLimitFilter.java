package com.cartumio.gate.config.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitService rateLimitService;

  public RateLimitFilter(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String ip = getIp(request);
    String path = request.getRequestURI();
    Bucket bucket = rateLimitService.resolveBucket(ip, path);

    log.debug("RateLimit started | ip={}, method={}, path={}", ip, request.getMethod(), path);

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
      return;
    }

    log.warn("RateLimit exceeded | ip={}, method={}, path={}", ip, request.getMethod(), path);

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getWriter()
        .write(
            """
                {
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded"
                }
                """);
  }

  private String getIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Fowarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}
