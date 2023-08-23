package com.legends.promiscuous.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${mail.api.key}")
    private String mailApiKey;
    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.dev.token}")
    private String testToken;

    public String getMailApiKey() {
        return mailApiKey;
    }
    @Value("${cloud.api.name}")
    private String cloudName;

    @Value("${cloud.api.key}")
    private String cloudKey;

    @Value("${cloud.api.secret}")
    private String cloudSecret;

    public String getTestToken() {
        return testToken;
    }
    public String getBaseUrl(){
        return baseUrl; }

    public String getCloudName() {
        return cloudName;
    }

    public String getCloudKey() {
        return cloudKey;
    }

    public String getCloudSecret() {
        return cloudSecret;
    }
}
