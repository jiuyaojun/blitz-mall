package com.blitz.security.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks login attempts per account and per IP for brute-force protection.
 * <p>
 * Lockout policy:
 * - 5 consecutive failed attempts → account locked for 15 minutes
 * - IP-level rate limiting: max N requests per minute per endpoint
 * <p>
 * This is an in-memory implementation. For production, use Redis to share
 * state across instances.
 */
@Service
public class LoginAttemptService {

    /** Max failed login attempts before account lockout */
    private static final int MAX_ATTEMPTS = 5;

    /** Lockout duration in seconds */
    private static final long LOCKOUT_DURATION_SECONDS = 15 * 60;

    /** Per-account failed attempt tracking: username → (attempts, lockUntil) */
    private final Map<String, AttemptState> accountAttempts = new ConcurrentHashMap<>();

    /** Per-IP request counting: ip:path → (windowStart, count) */
    private final Map<String, WindowCounter> ipCounters = new ConcurrentHashMap<>();

    /* ========== Account Lockout ========== */

    /**
     * Records a failed login attempt for the given account.
     */
    public void recordFailedAttempt(String username) {
        AttemptState state = accountAttempts.computeIfAbsent(username, k -> new AttemptState());
        synchronized (state) {
            state.attempts++;
            if (state.attempts >= MAX_ATTEMPTS) {
                state.lockedUntil = Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS);
            }
        }
    }

    /**
     * Clears failed attempts after a successful login.
     */
    public void clearAttempts(String username) {
        accountAttempts.remove(username);
    }

    /**
     * Checks whether the account is currently locked.
     */
    public boolean isAccountLocked(String username) {
        AttemptState state = accountAttempts.get(username);
        if (state == null) return false;
        synchronized (state) {
            if (state.lockedUntil == null) return false;
            if (Instant.now().isAfter(state.lockedUntil)) {
                // Lockout expired — reset
                accountAttempts.remove(username);
                return false;
            }
            return true;
        }
    }

    /**
     * Returns remaining lockout seconds (0 if not locked).
     */
    public long getRemainingLockoutSeconds(String username) {
        AttemptState state = accountAttempts.get(username);
        if (state == null || state.lockedUntil == null) return 0;
        long remaining = state.lockedUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }

    /* ========== IP Rate Limiting ========== */

    /**
     * Checks whether the given IP has exceeded the rate limit for a path.
     * Uses a simple fixed-window counter (1-minute windows).
     *
     * @return true if rate-limited, false if allowed
     */
    public boolean isIpRateLimited(String ip, String path, int maxRequests) {
        String key = ip + ":" + path;
        WindowCounter counter = ipCounters.computeIfAbsent(key, k -> new WindowCounter());
        synchronized (counter) {
            Instant now = Instant.now();
            // Reset window if 60 seconds have passed
            if (counter.windowStart == null ||
                now.getEpochSecond() - counter.windowStart.getEpochSecond() >= 60) {
                counter.windowStart = now;
                counter.count = 1;
                return false;
            }
            counter.count++;
            return counter.count > maxRequests;
        }
    }

    /* ========== Internal State Classes ========== */

    private static class AttemptState {
        int attempts = 0;
        Instant lockedUntil = null;
    }

    private static class WindowCounter {
        Instant windowStart = null;
        int count = 0;
    }
}
