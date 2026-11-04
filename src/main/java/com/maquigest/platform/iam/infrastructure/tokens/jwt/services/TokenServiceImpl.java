package com.maquigest.platform.iam.infrastructure.tokens.jwt.services;

import com.maquigest.platform.iam.application.internal.outboundservices.tokens.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

/** JWT adapter: issue signed and time-limited tokens, validate signatures and expiration. */
@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final SecretKey signingKey;
    private final int expirationDays;

    public TokenServiceImpl(
            @Value("${authorization.jwt.secret}") String secret,
            @Value("${authorization.jwt.expiration.days}") int expirationDays) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expirationDays <= 0) {
            throw new IllegalArgumentException("JWT expiration days must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationDays = expirationDays;
    }

    @Override
    public String generateToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT subject email is required");
        }
        var issuedAt = Instant.now();
        var expiresAt = issuedAt.plus(expirationDays, ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            var claims = extractAllClaims(token);
            return claims.getSubject() != null && !claims.getSubject().isBlank();
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: {}", ex.getClass().getSimpleName());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
    }
}
