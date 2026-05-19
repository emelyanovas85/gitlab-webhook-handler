package ru.cbr.bugbusters.gitwebhookhandler.service.handlers.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabWebhookService {

    private final List<GitLabEventHandler> handlers;

    @Value("${app.gitlab.webhook-token:}")
    private String secretToken;

    public void process(String eventType, String token, JsonNode payload) {
        if (secretToken != null && !secretToken.isBlank()) {
            if (!secretToken.equals(token)) {
                throw new SecurityException("Invalid GitLab webhook token");
            }
        }

        if (eventType == null || eventType.isBlank()) {
            log.warn("[GitLab] Received webhook without event type header");
            return;
        }

        handlers.stream()
                .filter(h -> h.supports(eventType))
                .forEach(h -> {
                    log.info("[GitLab] Handling event '{}' with {}", eventType, h.getClass().getSimpleName());
                    h.handle(payload);
                });
    }
}
