package com.eatnow.backend.auth.security;

import com.eatnow.backend.config.JwtProperties;
import com.eatnow.backend.user.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokenPair(SysUser user, List<String> roles) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(jwtProperties.getAccessTokenExpireMinutes(), ChronoUnit.MINUTES);
        Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshTokenExpireDays(), ChronoUnit.DAYS);

        String accessToken = buildToken(user, roles, JwtTokenType.ACCESS, now, accessExpiresAt);
        String refreshToken = buildToken(user, roles, JwtTokenType.REFRESH, now, refreshExpiresAt);
        return new TokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        Claims claims = parseToken(token, JwtTokenType.ACCESS);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get(CLAIM_USERNAME, String.class);
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        return new AuthenticatedUser(userId, username, roles == null ? List.of() : roles.stream().map(String::valueOf).toList());
    }

    public Claims parseToken(String token, JwtTokenType expectedType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!expectedType.name().equals(tokenType)) {
            throw new JwtException("invalid token type");
        }
        return claims;
    }

    public Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims;
    }

    public JwtTokenType getTokenType(String token) {
        Claims claims = parseClaims(token);
        return JwtTokenType.valueOf(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    private String buildToken(
            SysUser user,
            List<String> roles,
            JwtTokenType tokenType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TOKEN_TYPE, tokenType.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    @Getter
    @RequiredArgsConstructor
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final Instant accessExpiresAt;
        private final Instant refreshExpiresAt;
    }
}
