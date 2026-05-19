package ru.cbr.bugbusters.gitwebhookhandler.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Common interface for all webhook event handlers.
 * Implementations are auto-discovered by Spring via @Component.
 */
public interface WebhookEventHandler {

    boolean supports(String eventType);

    void handle(JsonNode payload);
}
