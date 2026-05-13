package com.example.gitlabwebhookhandler.service.handler;

import com.example.gitlabwebhookhandler.service.GitLabEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MergeRequestEventHandler implements GitLabEventHandler {

    private static final String MR_HOOK = "Merge Request Hook";

    @Override
    public boolean supports(String eventType) {
        return MR_HOOK.equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(JsonNode payload) {
        JsonNode attrs       = payload.path("object_attributes");
        String   title       = attrs.path("title").asText("unknown");
        String   state       = attrs.path("state").asText("unknown");
        String   sourceBranch = attrs.path("source_branch").asText("unknown");
        String   targetBranch = attrs.path("target_branch").asText("unknown");
        String   authorName  = payload.path("user").path("name").asText("unknown");

        log.info("[MERGE REQUEST] Title: '{}', State: {}, {}->{}, Author: {}",
                title, state, sourceBranch, targetBranch, authorName);

        // TODO: add your business logic here
        // e.g. auto-assign reviewer, check code coverage, post comment, etc.
    }
}
