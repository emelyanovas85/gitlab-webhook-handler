package com.example.gitlabwebhookhandler.controller;

import com.example.gitlabwebhookhandler.model.GitLabEvent;
import com.example.gitlabwebhookhandler.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Accepts GitLab webhook events.
     * GitLab sends the event type in the header X-Gitlab-Event.
     * Optionally secured by X-Gitlab-Token.
     */
    @PostMapping("/gitlab")
    public ResponseEntity<String> handleGitLabWebhook(
            @RequestHeader(value = "X-Gitlab-Event", required = false) String eventType,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestBody JsonNode payload) {

        log.info("Received GitLab webhook. Event: {}", eventType);
        log.debug("Payload: {}", payload);

        webhookService.process(eventType, token, payload);

        return ResponseEntity.ok("Webhook processed");
    }
}
