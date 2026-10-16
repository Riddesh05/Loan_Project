package com.example.Loan_Project.Filter;

import com.example.Loan_Project.ServiceImpl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        boolean isTwoFactorEndpoint =
                path.equals("/api/v1/customers/2fa/setup")
                        || path.equals("/api/v1/customers/2fa/verify");

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No token: let Spring Security decide whether
        // this endpoint is public or requires authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);

            String tokenType = claims.get("tokenType", String.class);
            String accountType = claims.get("accountType", String.class);
            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token subject"
                );
                return;
            }

            // Pending tokens must never authenticate a normal API.

            if ("2FA_PENDING".equals(tokenType)) {

                boolean isCustomer2FA =
                        "CUSTOMER".equals(accountType)
                                && isTwoFactorEndpoint;

                boolean isOfficer2FA =
                        "OFFICER".equals(accountType)
                                && (
                                path.equals("/api/v1/officer/2fa/setup")
                                        || path.equals("/api/v1/officer/2fa/verify")
                        );

                if (!isCustomer2FA && !isOfficer2FA) {
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Pending token is only allowed for 2FA endpoints"
                    );
                    return;
                }

                filterChain.doFilter(request, response);
                return;
            }


            // Only access tokens can authenticate normal APIs.
            if (!"ACCESS".equals(tokenType)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token type"
                );
                return;
            }

            if (isTwoFactorEndpoint) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Use a pending token for 2FA endpoints"
                );
                return;
            }

            String role = claims.get("role", String.class);

            if (role == null || role.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token has no role"
                );
                return;
            }

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(
                                    role.startsWith("ROLE_")
                                            ? role
                                            : "ROLE_" + role
                            ))
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/api/v1/customers/register")
                || path.equals("/api/v1/customers/verify-otp")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/customers/2fa/setup")
                || path.equals("/api/v1/customers/2fa/verify")
                || path.equals("/api/v1/officer/login")
                || path.equals("/api/v1/officer/2fa/setup")
                || path.equals("/api/v1/officer/2fa/verify");
    }
}
