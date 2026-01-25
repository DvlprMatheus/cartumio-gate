package com.cartumio.gate.config.ratelimit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.bucket4j.Bucket;

@DisplayName("RateLimitService - Tests")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    private RateLimitProperties properties;

    private static final String IP = "192.168.1.100";
    private static final String PATH = "/gate/v1/waitlist-users/create";
    private static final int CAPACITY = 10;
    private static final Duration REFILL = Duration.ofMinutes(1);

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();

        RateLimitProperties.Rule defaultRule = new RateLimitProperties.Rule();
        defaultRule.setPath("default");
        defaultRule.setCapacity(CAPACITY);
        defaultRule.setRefill(REFILL);
        properties.setDefaultRule(defaultRule);

        List<RateLimitProperties.Rule> rules = new ArrayList<>();
        properties.setRules(rules);

        rateLimitService = new RateLimitService(properties);
    }

    @Test
    @DisplayName("Should resolve bucket using default rule when no matching rule found")
    void testResolveBucketWithDefaultRule() {
        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == CAPACITY : "Bucket should have default capacity";
    }

    @Test
    @DisplayName("Should resolve bucket using matching rule when path matches")
    void testResolveBucketWithMatchingRule() {
        RateLimitProperties.Rule customRule = new RateLimitProperties.Rule();
        customRule.setPath("/gate/v1/waitlist-users/.*");
        customRule.setCapacity(5);
        customRule.setRefill(Duration.ofMinutes(2));
        properties.getRules().add(customRule);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == 5 : "Bucket should have custom rule capacity";
    }

    @Test
    @DisplayName("Should return same bucket instance for same IP and path")
    void testResolveBucketReturnsSameInstance() {
        Bucket bucket1 = rateLimitService.resolveBucket(IP, PATH);
        Bucket bucket2 = rateLimitService.resolveBucket(IP, PATH);

        assertSame(bucket1, bucket2, "Should return same bucket instance");
    }

    @Test
    @DisplayName("Should return different bucket instances for different IPs")
    void testResolveBucketReturnsDifferentInstanceForDifferentIP() {
        String ip1 = "192.168.1.100";
        String ip2 = "192.168.1.200";

        Bucket bucket1 = rateLimitService.resolveBucket(ip1, PATH);
        Bucket bucket2 = rateLimitService.resolveBucket(ip2, PATH);

        assert bucket1 != bucket2 : "Should return different bucket instances";
    }

    @Test
    @DisplayName("Should return different bucket instances for different paths with same IP when using default rule")
    void testResolveBucketReturnsDifferentInstanceForDifferentPathWithDefaultRule() {
        String path1 = "/gate/v1/waitlist-users/create";
        String path2 = "/gate/v1/other-endpoint";

        Bucket bucket1 = rateLimitService.resolveBucket(IP, path1);
        Bucket bucket2 = rateLimitService.resolveBucket(IP, path2);

        assert bucket1 != bucket2 : "Should return different bucket instances for different paths";
    }

    @Test
    @DisplayName("Should use first matching rule when multiple rules match")
    void testResolveBucketUsesFirstMatchingRule() {
        RateLimitProperties.Rule rule1 = new RateLimitProperties.Rule();
        rule1.setPath("/gate/.*");
        rule1.setCapacity(3);
        rule1.setRefill(Duration.ofMinutes(1));

        RateLimitProperties.Rule rule2 = new RateLimitProperties.Rule();
        rule2.setPath("/gate/v1/.*");
        rule2.setCapacity(7);
        rule2.setRefill(Duration.ofMinutes(1));

        properties.getRules().add(rule1);
        properties.getRules().add(rule2);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == 3 : "Should use first matching rule";
    }

    @Test
    @DisplayName("Should create bucket with correct capacity from rule")
    void testResolveBucketCreatesBucketWithCorrectCapacity() {
        RateLimitProperties.Rule customRule = new RateLimitProperties.Rule();
        customRule.setPath("/gate/.*");
        customRule.setCapacity(20);
        customRule.setRefill(Duration.ofMinutes(5));
        properties.getRules().add(customRule);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == 20 : "Bucket should have correct capacity";
    }

    @Test
    @DisplayName("Should use default rule when rules is null")
    void testResolveBucketWithNullRules() {
        properties.setRules(null);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == CAPACITY : "Bucket should have default capacity when rules is null";
    }

    @Test
    @DisplayName("Should use default rule when rules is empty")
    void testResolveBucketWithEmptyRules() {
        properties.setRules(new ArrayList<>());

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == CAPACITY : "Bucket should have default capacity when rules is empty";
    }

    @Test
    @DisplayName("Should throw IllegalStateException when default rule is null")
    void testResolveBucketThrowsExceptionWhenDefaultRuleIsNull() {
        properties.setDefaultRule(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> rateLimitService.resolveBucket(IP, PATH)
        );

        assert exception.getMessage().contains("Rate limit rule not found and default rule is not configured")
            : "Exception message should indicate default rule is not configured";
    }

    @Test
    @DisplayName("Should create bucket with correct capacity when default rule path is null")
    void testResolveBucketWithNullDefaultRulePath() {
        RateLimitProperties.Rule defaultRule = new RateLimitProperties.Rule();
        defaultRule.setPath(null);
        defaultRule.setCapacity(CAPACITY);
        defaultRule.setRefill(REFILL);
        properties.setDefaultRule(defaultRule);
        properties.setRules(null);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == CAPACITY : "Bucket should be created even when default rule path is null";
    }

    @Test
    @DisplayName("Should use default rule when no rules match and rules list is not empty")
    void testResolveBucketUsesDefaultRuleWhenNoMatch() {
        RateLimitProperties.Rule nonMatchingRule = new RateLimitProperties.Rule();
        nonMatchingRule.setPath("/other/.*");
        nonMatchingRule.setCapacity(15);
        nonMatchingRule.setRefill(Duration.ofMinutes(3));
        properties.getRules().add(nonMatchingRule);

        Bucket bucket = rateLimitService.resolveBucket(IP, PATH);

        assertNotNull(bucket);
        assert bucket.getAvailableTokens() == CAPACITY : "Bucket should use default rule when no rules match";
    }

    @Test
    @DisplayName("Should return different bucket instances for different paths even when using same rule")
    void testResolveBucketReturnsDifferentInstancesForDifferentPathsWithSameRule() {
        RateLimitProperties.Rule customRule = new RateLimitProperties.Rule();
        customRule.setPath("/gate/.*");
        customRule.setCapacity(20);
        customRule.setRefill(Duration.ofMinutes(5));
        properties.getRules().add(customRule);

        String path1 = "/gate/v1/waitlist-users/create";
        String path2 = "/gate/v1/waitlist-users/confirm";

        Bucket bucket1 = rateLimitService.resolveBucket(IP, path1);
        Bucket bucket2 = rateLimitService.resolveBucket(IP, path2);

        assert bucket1 != bucket2 : "Should return different bucket instances for different paths even with same rule";
        assert bucket1.getAvailableTokens() == 20 : "First bucket should have correct capacity";
        assert bucket2.getAvailableTokens() == 20 : "Second bucket should have correct capacity";
    }
}
