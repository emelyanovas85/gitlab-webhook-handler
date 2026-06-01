package ru.cbr.bugbusters.gitwebhookhandler.webhook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.cbr.bugbusters.gitwebhookhandler.review.api.ReviewTriggerCommand;
import ru.cbr.bugbusters.gitwebhookhandler.review.service.MrReviewOrchestrator;
import ru.cbr.bugbusters.gitwebhookhandler.webhook.domain.MergeRequestHookPayload;

/**
 * Обрабатывает Merge Request Hook от GitLab.
 * Запускает ревью для событий: opened, reopened, approved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MergeRequestHookHandler {

    private final MrReviewOrchestrator mrReviewOrchestrator;

    public void handle(MergeRequestHookPayload payload) {
        if (payload.objectAttributes() == null) {
            log.debug("Skipping MR hook without object_attributes");
            return;
        }

        String action = payload.objectAttributes().action();
        String state  = payload.objectAttributes().state();

        // Запускаем ревью только для открытия/переоткрытия/апрува MR
        if (!isReviewableAction(action, state)) {
            log.debug("Skipping MR hook: action={}, state={}", action, state);
            return;
        }

        Long projectId = payload.projectId();
        Long mrIid     = payload.objectAttributes().iid();

        if (projectId == null || mrIid == null) {
            log.warn("MR hook missing projectId or mrIid, skipping");
            return;
        }

        String lastCommit = payload.objectAttributes().lastCommit() != null
                ? payload.objectAttributes().lastCommit().id()
                : null;

        ReviewTriggerCommand command = new ReviewTriggerCommand(
                projectId,
                mrIid,
                payload.objectAttributes().sourceBranch(),
                payload.objectAttributes().targetBranch(),
                lastCommit,
                payload.objectAttributes().title(),
                payload.objectAttributes().url(),
                payload.user() != null ? payload.user().username() : "gitlab"
        );

        log.info("Triggering review: project={}, mrIid={}, action={}", projectId, mrIid, action);
        mrReviewOrchestrator.runReview(command);
    }

    private boolean isReviewableAction(String action, String state) {
        if (action == null) return false;
        return switch (action.toLowerCase()) {
            case "open", "reopen", "approved" -> true;
            default -> false;
        };
    }
}
