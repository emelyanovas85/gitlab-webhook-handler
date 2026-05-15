package ru.cbr.bugbusters.gitwebhookhandler.controllers;

import ru.cbr.bugbusters.gitwebhookhandler.service.handlers.gitlab.GitLabWebhookService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Tag(name = "GitLab Webhook", description = "Приём и обработка webhook-ов от GitLab")
public class GitLabWebhookController {

    private final GitLabWebhookService gitLabWebhookService;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Принять GitLab webhook",
            description = "Принимает события от GitLab: Push, Merge Request, Pipeline, Issue."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook успешно обработан",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Webhook processed"))),
            @ApiResponse(responseCode = "400", description = "Некорректный JSON в теле запроса")
    })
    @PostMapping("/gitlab")
    public ResponseEntity<String> handleGitLabWebhook(
            @Parameter(description = "Тип события GitLab", example = "Push Hook")
            @RequestHeader(value = "X-Gitlab-Event", required = false) String eventType,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload от GitLab",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "Push Hook", value = """
                                            {
                                              "ref": "refs/heads/main",
                                              "user_name": "john.doe",
                                              "total_commits_count": 3
                                            }"""),
                                    @ExampleObject(name = "Merge Request Hook", value = """
                                            {
                                              "object_attributes": {
                                                "title": "Fix bug",
                                                "state": "opened",
                                                "source_branch": "feature/fix",
                                                "target_branch": "main"
                                              }
                                            }""")
                            }))
            @RequestBody String rawPayload) {

        log.info("Received GitLab webhook. Event: {}", eventType);
        try {
            JsonNode payload = objectMapper.readTree(rawPayload);
            gitLabWebhookService.process(eventType, payload);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            return ResponseEntity.badRequest().body("Invalid JSON payload");
        }
    }
}
