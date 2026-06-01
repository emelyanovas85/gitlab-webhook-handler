package ru.cbr.bugbusters.gitwebhookhandler.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.cbr.bugbusters.gitwebhookhandler.common.config.AppProperties;
import ru.cbr.bugbusters.gitwebhookhandler.review.api.ReviewTriggerCommand;

import java.util.List;

/**
 * Клиент к граф-сервису.
 * Отправляет информацию о MR (ветка, коммит и т.п.) и получает List<String> —
 * список контекстов (фрагментов кода) для ревью.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphServiceClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    /**
     * Запрашивает у граф-сервиса контексты для ревью.
     *
     * @param command данные MR
     * @return список строк — каждая строка представляет контекст одной группы
     */
    public List<String> fetchContexts(ReviewTriggerCommand command) {
        String url = appProperties.graphService().url() + "/api/graph/contexts";
        try {
            List<String> contexts = restClient.post()
                    .uri(url)
                    .body(toRequest(command))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (contexts == null || contexts.isEmpty()) {
                log.warn("Graph-service returned empty contexts for MR {}", command.mrIid());
                return List.of();
            }
            log.info("Graph-service returned {} context(s) for MR {}", contexts.size(), command.mrIid());
            return contexts;
        } catch (Exception e) {
            log.error("Failed to fetch contexts from graph-service for MR {}: {}", command.mrIid(), e.getMessage(), e);
            return List.of();
        }
    }

    private MrContextRequest toRequest(ReviewTriggerCommand cmd) {
        return new MrContextRequest(
                cmd.projectId(),
                cmd.mrIid(),
                cmd.sourceBranch(),
                cmd.targetBranch(),
                cmd.lastCommit()
        );
    }

    /**
     * DTO запроса к граф-сервису.
     */
    public record MrContextRequest(
            Long projectId,
            Long mrIid,
            String sourceBranch,
            String targetBranch,
            String lastCommit
    ) {}
}
