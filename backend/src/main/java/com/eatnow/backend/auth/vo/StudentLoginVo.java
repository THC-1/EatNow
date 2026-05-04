package com.eatnow.backend.auth.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentLoginVo {

    Long userId;
    Boolean isNewUser;
    @JsonUnwrapped
    TokenPairVo tokenPair;
}
