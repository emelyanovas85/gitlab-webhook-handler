package ru.cbr.bugbusters.gitwebhookhandler.review.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.cbr.bugbusters.gitwebhookhandler.common.config.AppProperties;

/**
 * Тулы граф-сервиса, доступные LLM во время ревью.
 * Каждый экземпляр создаётся под конкретный контекст ревью (scope=prototype).
 * <p>
 * LLM может вызвать тул для получения дополнительного контекста:
 * - getRelatedCode   — получить код связанных классов/методов
 * - getFileHistory   — получить историю изменений файла
 */
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class GraphServiceToolsProvider {

    private final RestClient restClient;
    private final AppProperties appProperties;

    @Tool(description = "Get source code of a related class or method from the repository via graph-service. Use when you need to understand a dependency referenced in the reviewed code.")
    public String getRelatedCode(
            @ToolParam(description = "Fully qualified class name or method reference, e.g. 'ru.cbr.bugbusters.SomeService' or 'ru.cbr.bugbusters.SomeService#doWork'") String reference
    ) {
        log.info("[Tool] getRelatedCode called with reference={}", reference);
        try {
            return restClient.get()
                    .uri(appProperties.graphService().url() + "/api/graph/code?ref=" + reference)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("[Tool] getRelatedCode failed for ref={}: {}", reference, e.getMessage());
            return "Could not retrieve code for: " + reference;
        }
    }

    @Tool(description = "Get recent commit history and change summary for a specific file path from graph-service. Helps understand the evolution of the code being reviewed.")
    public String getFileHistory(
            @ToolParam(description = "File path relative to repo root, e.g. 'src/main/java/ru/cbr/SomeService.java'") String filePath
    ) {
        log.info("[Tool] getFileHistory called for filePath={}", filePath);
        try {
            return restClient.get()
                    .uri(appProperties.graphService().url() + "/api/graph/history?path=" + filePath)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("[Tool] getFileHistory failed for path={}: {}", filePath, e.getMessage());
            return "Could not retrieve history for: " + filePath;
        }
    }
}
