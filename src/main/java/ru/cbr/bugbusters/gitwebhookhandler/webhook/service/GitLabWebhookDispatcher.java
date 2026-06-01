package ru.cbr.bugbusters.gitwebhookhandler.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.cbr.bugbusters.gitwebhookhandler.webhook.domain.MergeRequestHookPayload;

/**
 * Диспетчер GitLab webhook-ов.
 * Получает события от webhook-distributor-client и маршрутизирует к нужному хендлеру.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabWebhookDispatcher {

    private final ObjectMapper objectMapper;
    private final MergeRequestHookHandler mergeRequestHookHandler;

    public void dispatch(String eventType, String rawPayload) {
        try {
            if ("Merge Request Hook".equalsIgnoreCase(eventType)) {
                MergeRequestHookPayload payload = objectMapper.readValue(rawPayload, MergeRequestHookPayload.class);
                mergeRequestHookHandler.handle(payload);
                return;
            }
            log.debug("Ignoring unsupported GitLab event: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to dispatch GitLab webhook event={}", eventType, e);
        }
    }
}
