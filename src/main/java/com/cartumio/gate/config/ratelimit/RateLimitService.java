package com.cartumio.gate.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimitService {

  private final RateLimitProperties properties;
  private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();

  public RateLimitService(RateLimitProperties properties) {
    this.properties = properties;
  }

  public Bucket resolveBucket(String ip, String path) {
    RateLimitProperties.Rule rule;

    if (properties.getRules() == null || properties.getRules().isEmpty()) {
      rule = properties.getDefaultRule();
    } else {
      rule =
          properties.getRules().stream()
              .filter(r -> path.matches(r.getPath()))
              .findFirst()
              .orElse(properties.getDefaultRule());
    }

    if (rule == null) {
      throw new IllegalStateException(
          "Rate limit rule not found and default rule is not configured");
    }

    Bandwidth bandwidth =
        Bandwidth.builder()
            .capacity(rule.getCapacity())
            .refillIntervally(rule.getCapacity(), rule.getRefill())
            .build();

    String key = ip + ":" + path;
    return buckets.computeIfAbsent(
        key,
        k -> {
          log.info(
              "Creating RateLimit bucket | ip={}, path={}, capacity={}, refill={}",
              ip,
              path,
              rule.getCapacity(),
              rule.getRefill());

          return Bucket.builder().addLimit(bandwidth).build();
        });
  }
}
