package ru.cbr.bugbusters.gitwebhookhandler.webhook.domain;

public record ProjectInfo(
        Long id,
        String name,
        String description,
        String homepage
) {
}
