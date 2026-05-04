package com.eatnow.backend.auth.service;

import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.config.WechatProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WechatAuthClient {

    private final RestClient.Builder restClientBuilder;
    private final WechatProperties wechatProperties;
    private final ObjectMapper objectMapper;

    public WechatIdentity exchangeMiniProgramCode(String code) {
        WechatAppConfig config = resolveMiniProgramConfig();
        String responseBody;
        try {
            responseBody = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.weixin.qq.com")
                            .path("/sns/jscode2session")
                            .queryParam("appid", config.appId())
                            .queryParam("secret", config.appSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            log.warn("Wechat code2session request failed", exception);
            throw new BusinessException(
                    HttpStatus.BAD_GATEWAY,
                    "微信登录服务请求失败: " + exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                    exception
            );
        }

        WechatCode2SessionResponse response;
        try {
            response = objectMapper.readValue(responseBody, WechatCode2SessionResponse.class);
        } catch (JsonProcessingException exception) {
            log.warn("Wechat code2session response parse failed: {}", responseBody, exception);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "微信登录响应解析失败", exception);
        }

        if (response == null) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "微信登录失败，请稍后重试");
        }
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "微信登录失败: " + response.getErrmsg());
        }
        if (!StringUtils.hasText(response.getOpenid())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "未获取到微信 openid");
        }
        return new WechatIdentity(response.getOpenid(), response.getSessionKey());
    }

    private WechatAppConfig resolveMiniProgramConfig() {
        WechatProperties.App miniProgram = wechatProperties.getMiniProgram();
        if (miniProgram != null
                && StringUtils.hasText(miniProgram.getAppId())
                && StringUtils.hasText(miniProgram.getAppSecret())) {
            return validateConfig(miniProgram.getAppId(), miniProgram.getAppSecret(), "小程序微信 appId/appSecret 尚未配置");
        }
        return validateConfig(wechatProperties.getAppId(), wechatProperties.getAppSecret(), "微信 appId/appSecret 尚未配置");
    }

    private WechatAppConfig validateConfig(String appId, String appSecret, String errorMessage) {
        if (!StringUtils.hasText(appId)
                || !StringUtils.hasText(appSecret)
                || appId.startsWith("your-")
                || appSecret.startsWith("your-")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return new WechatAppConfig(appId, appSecret);
    }

    private record WechatAppConfig(String appId, String appSecret) {
    }

    public record WechatIdentity(String openid, String sessionKey) {
    }

    @Data
    public static class WechatCode2SessionResponse {
        private String openid;
        @JsonProperty("session_key")
        private String sessionKey;
        private Integer errcode;
        private String errmsg;
    }
}
