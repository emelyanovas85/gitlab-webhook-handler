package com.example.gitlabwebhookhandler.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class GitLabEvent {
    private String eventType;
    private JsonNode payload;
}
