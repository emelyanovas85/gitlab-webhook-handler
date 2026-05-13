# GitLab Webhook Handler

Spring Boot приложение для приёма и обработки webhook-событий от GitLab.

## Технологии

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Web MVC**
- **Spring Validation**
- **Spring Actuator**
- **Lombok**
- **Jackson**

## Архитектура

Проект использует паттерн **Chain of Responsibility** — каждый тип события обрабатывается своим `GitLabEventHandler`.

```
POST /api/webhook/gitlab
        │
        ▼
WebhookController
        │
        ▼
WebhookService  ─── проверка токена безопасности
        │
        ▼
[GitLabEventHandler]  ─── выбирает нужный обработчик
   ├── PushEventHandler          (Push Hook)
   ├── MergeRequestEventHandler  (Merge Request Hook)
   ├── PipelineEventHandler      (Pipeline Hook)
   └── IssueEventHandler         (Issue Hook)
```

## Запуск

```bash
./mvnw spring-boot:run
```

Либо с секретным токеном:

```bash
GITLAB_WEBHOOK_SECRET_TOKEN=mysecret ./mvnw spring-boot:run
```

## Эндпоинты

| Метод | URL | Описание |
|-------|-----|----------|
| POST  | `/api/webhook/gitlab` | Приём GitLab webhook |
| GET   | `/actuator/health`    | Healthcheck |
| GET   | `/actuator/info`      | Информация о приложении |

## Настройка GitLab

1. Откройте GitLab → Settings → Webhooks
2. URL: `http://your-server:8080/api/webhook/gitlab`
3. Secret Token: значение из `GITLAB_WEBHOOK_SECRET_TOKEN`
4. Выберите нужные события: Push, Merge Request, Pipeline, Issues
5. Нажмите **Add webhook**

## Поддерживаемые события

| X-Gitlab-Event Header  | Handler                     |
|------------------------|-----------------------------|
| `Push Hook`            | `PushEventHandler`          |
| `Merge Request Hook`   | `MergeRequestEventHandler`  |
| `Pipeline Hook`        | `PipelineEventHandler`      |
| `Issue Hook`           | `IssueEventHandler`         |

## Добавление нового обработчика

Создайте класс, реализующий `GitLabEventHandler`, и добавьте аннотацию `@Component`:

```java
@Slf4j
@Component
public class NoteEventHandler implements GitLabEventHandler {

    @Override
    public boolean supports(String eventType) {
        return "Note Hook".equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(JsonNode payload) {
        // ваша логика
    }
}
```

Spring автоматически добавит его в список обработчиков.

## Безопасность

Если задан `GITLAB_WEBHOOK_SECRET_TOKEN`, каждый запрос проверяется на наличие заголовка `X-Gitlab-Token`. При несовпадении возвращается `401 Unauthorized`.

## Тесты

```bash
./mvnw test
```
