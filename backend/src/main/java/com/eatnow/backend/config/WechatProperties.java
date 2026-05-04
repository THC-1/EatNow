package com.eatnow.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    /**
     * Legacy mini-program appId. Kept so existing deployments can upgrade without
     * immediately renaming configuration keys.
     */
    private String appId;

    /**
     * Legacy mini-program appSecret.
     */
    private String appSecret;

    private App miniProgram = new App();

    @Data
    public static class App {
        private String appId;
        private String appSecret;
    }
}
