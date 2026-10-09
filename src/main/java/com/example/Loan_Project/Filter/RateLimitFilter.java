package com.example.Loan_Project.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(RateLimitFilter.class);

    private static final long TIME_WINDOW = 60_000L;

    private final Map<String, RequestInfo> requestCounts =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Apply rate limiting only to REST APIs.
        if (!requestPath.startsWith("/api/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        int maxRequests = getMaxRequests(request);
        long currentTime = System.currentTimeMillis();

        // Atomically create or update the request counter.
        RequestInfo requestInfo = requestCounts.compute(
                clientIp,
                (ip, existing) -> {
                    if (existing == null
                            || currentTime - existing.getStartTime()
                            >= TIME_WINDOW) {
                        return new RequestInfo(1, currentTime);
                    }

                    existing.incrementRequestCount();
                    return existing;
                });

        // Reject requests exceeding the configured limit.
        if (requestInfo.getRequestCount() > maxRequests) {

            logger.warn(
                    "Rate limit exceeded: IP={}, method={}, path={}, count={}, limit={}",
                    clientIp,
                    request.getMethod(),
                    requestPath,
                    requestInfo.getRequestCount(),
                    maxRequests);

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
                    {
                      "success": false,
                      "message": "Rate limit exceeded. Try again later.",
                      "data": {
                        "limit": %d,
                        "window": "60 seconds",
                        "requestCount": %d,
                        "algorithm": "FIXED_TIME_WINDOW"
                      }
                    }
                    """.formatted(
                    maxRequests,
                    requestInfo.getRequestCount()));

            return;
        }

        filterChain.doFilter(request, response);
    }

    private int getMaxRequests(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Customer registration: 5 requests per minute
        if (path.equals("/api/v1/customers/register")
                && method.equalsIgnoreCase("POST")) {
            return 5;
        }

        // CIBIL calculation: 10 requests per minute
        if (path.startsWith("/api/v1/cibil/calculate/")
                && method.equalsIgnoreCase("POST")) {
            return 10;
        }

        // Fetch one customer's CIBIL report: 10 requests per minute
        if (path.startsWith("/api/v1/cibil/customer/")
                && method.equalsIgnoreCase("GET")) {
            return 10;
        }

        // Fetch all CIBIL reports: 10 requests per minute
        if (path.equals("/api/v1/cibil/all")
                && method.equalsIgnoreCase("GET")) {
            return 10;
        }

        // All other APIs: 30 requests per minute
        return 30;
    }

    private static class RequestInfo {

        private int requestCount;
        private long startTime;

        public RequestInfo(int requestCount, long startTime) {
            this.requestCount = requestCount;
            this.startTime = startTime;
        }

        public synchronized int getRequestCount() {
            return requestCount;
        }

        public synchronized long getStartTime() {
            return startTime;
        }

        public synchronized void incrementRequestCount() {
            requestCount++;
        }
    }
}
