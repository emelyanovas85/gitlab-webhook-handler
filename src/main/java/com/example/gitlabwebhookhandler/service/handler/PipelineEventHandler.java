package com.example.gitlabwebhookhandler.service.handler;

import com.example.gitlabwebhookhandler.service.GitLabEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PipelineEventHandler implements GitLabEventHandler {

    private static final String PIPELINE_HOOK = "Pipeline Hook";

    @Override
    public boolean supports(String eventType) {
        return PIPELINE_HOOK.equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(JsonNode payload) {
        JsonNode attrs   = payload.path("object_attributes");
        String   status  = attrs.path("status").asText("unknown");
        String   ref     = attrs.path("ref").asText("unknown");
        long     duration = attrs.path("duration").asLong(0);
        String   project = payload.path("project").path("name").asText("unknown");

        log.info("[PIPELINE] Project: {}, Branch: {}, Status: {}, Duration: {}s",
                project, ref, status, duration);

        // TODO: add your business logic here
        // e.g. send alert on failure, update dashboard, trigger deployment, etc.
    }
}
