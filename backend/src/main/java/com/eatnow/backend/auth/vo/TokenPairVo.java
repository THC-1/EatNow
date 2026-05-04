package com.eatnow.backend.auth.vo;

import com.eatnow.backend.auth.security.JwtTokenProvider;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenPairVo {

    String token;
    String refreshToken;
    long tokenExpiresAt;
    long refreshTokenExpiresAt;

    public static TokenPairVo from(JwtTokenProvider.TokenPair tokenPair) {
        return TokenPairVo.builder()
                .token(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .tokenExpiresAt(tokenPair.getAccessExpiresAt().toEpochMilli())
                .refreshTokenExpiresAt(tokenPair.getRefreshExpiresAt().toEpochMilli())
                .build();
    }
}
