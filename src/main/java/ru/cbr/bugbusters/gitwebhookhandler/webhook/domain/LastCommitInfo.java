package ru.cbr.bugbusters.gitwebhookhandler.webhook.domain;

public record LastCommitInfo(
        String id,
        String message,
        String url
) {
}
