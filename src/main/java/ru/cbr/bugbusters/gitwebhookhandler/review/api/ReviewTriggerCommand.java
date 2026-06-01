package ru.cbr.bugbusters.gitwebhookhandler.review.api;

/**
 * Команда запуска ревью MR.
 * Формируется из MergeRequest Hook от GitLab (через webhook-distributor-client).
 */
public record ReviewTriggerCommand(
        Long projectId,
        Long mrIid,
        String sourceBranch,
        String targetBranch,
        String lastCommit,
        String mrTitle,
        String mrUrl,
        String triggeredBy
) {
}
