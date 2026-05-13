package com.example.gitlabwebhookhandler.service.handler;

import com.example.gitlabwebhookhandler.service.GitLabEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushEventHandler implements GitLabEventHandler {

    private static final String PUSH_HOOK = "Push Hook";

    @Override
    public boolean supports(String eventType) {
        return PUSH_HOOK.equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(JsonNode payload) {
        String ref         = payload.path("ref").asText("unknown");
        String projectName = payload.path("project").path("name").asText("unknown");
        String userName    = payload.path("user_name").asText("unknown");
        int    commitCount = payload.path("total_commits_count").asInt(0);

        log.info("[PUSH] Project: {}, Branch: {}, Author: {}, Commits: {}",
                projectName, ref, userName, commitCount);

        // TODO: add your business logic here
        // e.g. trigger CI, send notification, update database, etc.
    }
}
