package ru.cbr.bugbusters.gitwebhookhandler.review.domain;

/**
 * Результат ревью одного контекста (группы), полученного от граф-сервиса.
 */
public record GroupReviewResult(
        int index,
        boolean success,
        String reviewText
) {
    public static GroupReviewResult success(int index, String reviewText) {
        return new GroupReviewResult(index, true, reviewText);
    }

    public static GroupReviewResult failure(int index, String errorMessage) {
        return new GroupReviewResult(index, false, "Error: " + errorMessage);
    }
}
