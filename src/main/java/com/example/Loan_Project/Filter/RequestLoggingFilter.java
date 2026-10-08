package com.example.Loan_Project.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class RequestLoggingFilter
        extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime =
                System.currentTimeMillis();

        try {

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            long executionTime =
                    System.currentTimeMillis()
                            - startTime;

            System.out.println(
                    "API: "
                            + request.getMethod()
                            + " "
                            + request.getRequestURI()
                            + " | Status: "
                            + response.getStatus()
                            + " | Time: "
                            + executionTime
                            + " ms"
            );
        }
    }
}