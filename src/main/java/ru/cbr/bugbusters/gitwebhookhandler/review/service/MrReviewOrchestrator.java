package ru.cbr.bugbusters.gitwebhookhandler.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.cbr.bugbusters.gitwebhookhandler.review.api.ReviewTriggerCommand;
import ru.cbr.bugbusters.gitwebhookhandler.review.domain.GroupReviewResult;
import ru.cbr.bugbusters.gitwebhookhandler.review.service.GitLabNotesPublisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

/**
 * Оркестрирует процесс ревью MR:
 * 1. Запрашивает контексты у граф-сервиса (List<String>)
 * 2. Параллельно запускает LLM-ревью для каждого контекста
 * 3. Агрегирует результаты и публикует комментарий в GitLab
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrReviewOrchestrator {

    private final GraphServiceClient graphServiceClient;
    private final LlmReviewService llmReviewService;
    private final MarkdownCommentFormatter markdownCommentFormatter;
    private final GitLabNotesPublisher gitLabNotesPublisher;
    @Qualifier("reviewExecutor")
    private final Executor reviewExecutor;

    @Async("reviewExecutor")
    public void runReview(ReviewTriggerCommand command) {
        log.info("Starting async review for project={}, mrIid={}", command.projectId(), command.mrIid());

        List<String> contexts = graphServiceClient.fetchContexts(command);
        if (contexts.isEmpty()) {
            gitLabNotesPublisher.postNote(
                    command.projectId(),
                    command.mrIid(),
                    "**AI Review**: graph-service returned no context for analysis.");
            return;
        }

        // Параллельный запуск ревью: каждый контекст — в отдельный ChatClient
        List<CompletableFuture<GroupReviewResult>> futures = IntStream
                .range(0, contexts.size())
                .mapToObj(i -> CompletableFuture.supplyAsync(
                        () -> llmReviewService.review(i, contexts.get(i)),
                        reviewExecutor))
                .toList();

        List<GroupReviewResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        String comment = markdownCommentFormatter.format(command, results);
        gitLabNotesPublisher.postNote(command.projectId(), command.mrIid(), comment);
    }
}
