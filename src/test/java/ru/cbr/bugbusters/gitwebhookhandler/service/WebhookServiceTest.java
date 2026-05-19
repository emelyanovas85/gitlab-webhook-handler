package ru.cbr.bugbusters.gitwebhookhandler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cbr.bugbusters.gitwebhookhandler.service.handlers.gitlab.GitLabEventHandler;
import ru.cbr.bugbusters.gitwebhookhandler.service.handlers.gitlab.GitLabWebhookService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private GitLabEventHandler handlerA;

    @Mock
    private GitLabEventHandler handlerB;

    private GitLabWebhookService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new GitLabWebhookService(List.of(handlerA, handlerB));
    }

    @Test
    void shouldDelegateToMatchingHandler() {
        ObjectNode payload = mapper.createObjectNode();
        when(handlerA.supports("Push Hook")).thenReturn(true);
        when(handlerB.supports("Push Hook")).thenReturn(false);

        service.process("Push Hook", null, payload);

        verify(handlerA).handle(payload);
        verify(handlerB, never()).handle(any());
    }

    @Test
    void shouldDelegateToAllMatchingHandlers() {
        ObjectNode payload = mapper.createObjectNode();
        when(handlerA.supports("Push Hook")).thenReturn(true);
        when(handlerB.supports("Push Hook")).thenReturn(true);

        service.process("Push Hook", null, payload);

        verify(handlerA).handle(payload);
        verify(handlerB).handle(payload);
    }

    @Test
    void shouldNotCallAnyHandlerWhenEventTypeIsNull() {
        service.process(null, null, mapper.createObjectNode());
        verify(handlerA, never()).handle(any());
        verify(handlerB, never()).handle(any());
    }

    @Test
    void shouldNotCallAnyHandlerWhenEventTypeIsBlank() {
        service.process("  ", null, mapper.createObjectNode());
        verify(handlerA, never()).handle(any());
        verify(handlerB, never()).handle(any());
    }

    @Test
    void shouldThrowSecurityExceptionWhenTokenMismatch() {
        service = createServiceWithToken("correct-token");

        assertThatThrownBy(() -> service.process("Push Hook", "wrong-token", mapper.createObjectNode()))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid GitLab webhook token");
    }

    @Test
    void shouldPassWhenTokenMatchesSecret() {
        service = createServiceWithToken("correct-token");
        when(handlerA.supports("Push Hook")).thenReturn(true);

        service.process("Push Hook", "correct-token", mapper.createObjectNode());

        verify(handlerA).handle(any());
    }

    @Test
    void shouldSkipTokenValidationWhenSecretNotConfigured() {
        when(handlerA.supports("Push Hook")).thenReturn(false);
        when(handlerB.supports("Push Hook")).thenReturn(false);

        service.process("Push Hook", "any-token", mapper.createObjectNode());
    }

    private GitLabWebhookService createServiceWithToken(String token) {
        GitLabWebhookService svc = new GitLabWebhookService(List.of(handlerA, handlerB));
        try {
            var field = GitLabWebhookService.class.getDeclaredField("secretToken");
            field.setAccessible(true);
            field.set(svc, token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return svc;
    }
}
