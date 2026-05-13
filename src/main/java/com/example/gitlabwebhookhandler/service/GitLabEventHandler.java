package com.example.gitlabwebhookhandler.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface GitLabEventHandler {

    /**
     * Returns true if this handler supports the given GitLab event type.
     * Event type comes from the X-Gitlab-Event header (e.g. "Push Hook", "Merge Request Hook").
     */
    boolean supports(String eventType);

    /**
     * Handles the GitLab event payload.
     */
    void handle(JsonNode payload);
}
