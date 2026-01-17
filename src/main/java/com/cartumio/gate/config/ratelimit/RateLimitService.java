package com.cartumio.gate.config.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RateLimitService {
    
    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public Bucket resolveBucket(String ip, String path) {
        RateLimitProperties.Rule rule = properties.getRules().stream()
            .filter(r -> path.matches(r.getPath()))
            .findFirst()
            .orElse(properties.getDefaultRule());
        
        Bandwidth bandwidth = Bandwidth.builder()
            .capacity(rule.getCapacity())
            .refillIntervally(rule.getCapacity(), rule.getRefill())
            .build();
        
        String key = ip + ":" + rule.getPath();
        return buckets.computeIfAbsent(key, k -> {
            log.info(
                "Creating RateLimit bucket | ip={}, path={}, capacity={}, refill={}",
                ip,
                rule.getPath(),
                rule.getCapacity(),
                rule.getRefill()
            );
        
            return Bucket.builder().addLimit(bandwidth).build();
        });
    }
}
