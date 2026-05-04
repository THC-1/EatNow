package com.eatnow.backend.auth.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AccountLoginVo {

    Long userId;
    List<String> roles;
    @JsonUnwrapped
    TokenPairVo tokenPair;
}
