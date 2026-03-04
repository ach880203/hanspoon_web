package com.project.hanspoon.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "portone")
@Getter
@Setter
public class PortOneConfig {

    private String storeId;
    private String apiSecret;
    private ChannelKeys channelKey;

    @Getter
    @Setter
    public static class ChannelKeys {
        private String kakao;
        private String toss;
        private String tossPayments;
    }

    @Bean
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.portone.io")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
