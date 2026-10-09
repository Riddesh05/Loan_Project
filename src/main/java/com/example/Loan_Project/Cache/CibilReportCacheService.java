package com.example.Loan_Project.Cache;

import com.example.Loan_Project.DTO.CibilCalculationResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory cache for the latest CIBIL response per customer.
 * Suitable for the current single-instance application. If the application
 * is deployed across multiple instances, replace this with Redis.
 */
@Service
public class CibilReportCacheService {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public CibilCalculationResponse get(Long customerId) {
        CacheEntry entry = cache.get(customerId);
        if (entry == null) {
            return null;
        }

        if (entry.expiresAt().isBefore(Instant.now())) {
            cache.remove(customerId, entry);
            return null;
        }

        return entry.response();
    }

    public void put(Long customerId, CibilCalculationResponse response) {
        cache.put(customerId, new CacheEntry(response, Instant.now().plus(TTL)));
    }

    public void evict(Long customerId) {
        cache.remove(customerId);
    }

    private record CacheEntry(CibilCalculationResponse response, Instant expiresAt) {
    }
}
