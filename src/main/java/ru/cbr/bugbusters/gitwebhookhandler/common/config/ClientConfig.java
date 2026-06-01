package ru.cbr.bugbusters.gitwebhookhandler.common.config;

import org.gitlab4j.api.GitLabApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean
    public GitLabApi gitLabApi(AppProperties properties) {
        return new GitLabApi(properties.gitlab().url(), properties.gitlab().token());
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    /**
     * ChatClient.Builder используется для создания изолированных ChatClient
     * для каждого контекста (в MrReviewOrchestrator).
     * @Lazy — позволяет стартовать без реального OPENAI_API_KEY.
     */
    @Bean
    @Lazy
    public ChatClient.Builder chatClientBuilder(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
