package ru.cbr.bugbusters.gitwebhookhandler.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import ru.cbr.bugbusters.gitwebhookhandler.common.config.AppProperties;
import ru.cbr.bugbusters.gitwebhookhandler.review.domain.GroupReviewResult;

/**
 * Запускает LLM-ревью для одного контекста.
 * Каждый вызов создаёт изолированный ChatClient с тулами граф-сервиса.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmReviewService {

    private final ChatClient.Builder chatClientBuilder;
    private final AppProperties appProperties;
    // ObjectProvider позволяет получать prototype-бины (по одному на каждый контекст)
    private final ObjectProvider<GraphServiceToolsProvider> toolsProviderFactory;

    /**
     * Выполняет ревью одного контекста.
     *
     * @param index   порядковый номер контекста
     * @param context текст контекста (код для ревью), полученный от граф-сервиса
     * @return результат ревью
     */
    public GroupReviewResult review(int index, String context) {
        try {
            GraphServiceToolsProvider tools = toolsProviderFactory.getObject();
            String response = chatClientBuilder.build()
                    .prompt()
                    .system(appProperties.ai().systemPrompt())
                    .user(buildPrompt(index, context))
                    .tools(tools)
                    .call()
                    .content();
            return GroupReviewResult.success(index, response == null ? "No issues found." : response);
        } catch (Exception e) {
            log.error("LLM request failed for context index={}", index, e);
            return GroupReviewResult.failure(index, e.getMessage());
        }
    }

    private String buildPrompt(int index, String context) {
        return """
                ## Context group #%d

                %s
                """.formatted(index + 1, context);
    }
}
