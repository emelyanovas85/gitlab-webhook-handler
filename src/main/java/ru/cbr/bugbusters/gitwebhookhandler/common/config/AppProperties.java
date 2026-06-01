package ru.cbr.bugbusters.gitwebhookhandler.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        GitLabProperties gitlab,
        GraphServiceProperties graphService,
        AiProperties ai
) {
    public record GitLabProperties(String url, String token, String webhookToken) {}

    /**
     * graphServiceUrl — базовый URL граф-сервиса.
     * Пример: http://graph-service:8090
     */
    public record GraphServiceProperties(String url) {}

    public record AiProperties(String systemPrompt) {}
}
