package com.example.gitlabwebhookhandler.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    @Value("${gitlab.webhook.secret-token:}")
    private String secretToken;

    private final List<GitLabEventHandler> handlers;

    public void process(String eventType, String token, JsonNode payload) {
        validateToken(token);

        if (eventType == null || eventType.isBlank()) {
            log.warn("Received webhook without event type header");
            return;
        }

        handlers.stream()
                .filter(h -> h.supports(eventType))
                .forEach(h -> {
                    log.info("Handling event '{}' with handler: {}", eventType, h.getClass().getSimpleName());
                    h.handle(payload);
                });
    }

    private void validateToken(String token) {
        if (secretToken != null && !secretToken.isBlank()) {
            if (!secretToken.equals(token)) {
                throw new SecurityException("Invalid GitLab webhook token");
            }
        }
    }
}
