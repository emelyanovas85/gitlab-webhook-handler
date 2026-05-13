package com.example.gitlabwebhookhandler.service.handler;

import com.example.gitlabwebhookhandler.service.GitLabEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IssueEventHandler implements GitLabEventHandler {

    private static final String ISSUE_HOOK = "Issue Hook";

    @Override
    public boolean supports(String eventType) {
        return ISSUE_HOOK.equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(JsonNode payload) {
        JsonNode attrs  = payload.path("object_attributes");
        String   title  = attrs.path("title").asText("unknown");
        String   action = attrs.path("action").asText("unknown");
        String   author = payload.path("user").path("name").asText("unknown");

        log.info("[ISSUE] Action: {}, Title: '{}', Author: {}", action, title, author);

        // TODO: add your business logic here
    }
}
