
package com.example.Loan_Project.ServiceImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long pendingTokenExpiration = 5 * 60 * 1000L;
    private final long accessTokenExpiration = 60 * 60 * 1000L;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public String generatePendingToken(
            String email, String accountType) {

        return Jwts.builder()
                .subject(email)
                .claim("tokenType", "2FA_PENDING")
                .claim("accountType", accountType)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + pendingTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessToken(
            String email, String accountType, String role) {

        return Jwts.builder()
                .subject(email)
                .claim("tokenType", "ACCESS")
                .claim("accountType", accountType)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String validatePendingToken(String token) {

        Claims claims = extractClaims(token);

        String tokenType = claims.get("tokenType", String.class);
        String accountType = claims.get("accountType", String.class);

        if (!"2FA_PENDING".equals(tokenType)
                || !"CUSTOMER".equals(accountType)) {
            throw new IllegalArgumentException(
                    "Invalid 2FA pending token");
        }

        return claims.getSubject();
    }


    public String validateOfficerPendingToken(String token) {

        Claims claims = extractClaims(token);

        String tokenType = claims.get("tokenType", String.class);
        String accountType = claims.get("accountType", String.class);

        if (!"2FA_PENDING".equals(tokenType)
                || !"OFFICER".equals(accountType)) {
            throw new IllegalArgumentException(
                    "Invalid officer 2FA pending token");
        }

        return claims.getSubject();
    }

}
