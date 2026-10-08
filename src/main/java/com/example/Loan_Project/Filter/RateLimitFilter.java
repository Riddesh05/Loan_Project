package com.example.Loan_Project.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;

    private static final long TIME_WINDOW =
            60 * 1000L;

    private final Map<String, RequestInfo> requestMap =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        /*
         * Apply rate limiting only to
         * Support and Notification APIs.
         */
        if (!uri.startsWith("/api/v1/support")
                && !uri.startsWith("/api/v1/notifications")) {

            filterChain.doFilter(request, response);
            return;
        }

        String clientIp =
                request.getRemoteAddr();

        long currentTime =
                System.currentTimeMillis();

        RequestInfo requestInfo =
                requestMap.computeIfAbsent(
                        clientIp,
                        key -> new RequestInfo(currentTime)
                );

        synchronized (requestInfo) {

            /*
             * Reset counter after 60 seconds.
             */
            if (currentTime - requestInfo.startTime
                    >= TIME_WINDOW) {

                requestInfo.startTime = currentTime;
                requestInfo.requestCount = 0;
            }

            /*
             * Reject request when limit is reached.
             */
            if (requestInfo.requestCount
                    >= MAX_REQUESTS) {

                response.setStatus(429);

                response.setContentType(
                        "application/json"
                );

                response.getWriter().write(
                        """
                        {
                            "success": false,
                            "message": "Too many requests. Please try again later.",
                            "data": null
                        }
                        """
                );

                return;
            }

            requestInfo.requestCount++;
        }

        /*
         * Continue request.
         */
        filterChain.doFilter(
                request,
                response
        );
    }

    /*
     * Stores request information
     * for each client IP.
     */
    private static class RequestInfo {

        private long startTime;

        private int requestCount;

        private RequestInfo(long startTime) {
            this.startTime = startTime;
            this.requestCount = 0;
        }
    }
}