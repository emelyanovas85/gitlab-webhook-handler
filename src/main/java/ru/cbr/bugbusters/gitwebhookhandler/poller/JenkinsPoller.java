package ru.cbr.bugbusters.gitwebhookhandler.poller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;  // ← Spring, не Apache!
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class JenkinsPoller {

    private final JenkinsProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();
    private int lastSeenBuild = -1;

    public JenkinsPoller(JenkinsProperties props) {
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${jenkins.poll-interval-seconds}000")
    public void poll() {
        try {
            int current = getLastBuildNumber();
            if (current == lastSeenBuild) return;

            JsonNode build = getBuildInfo(current);
            if (build.path("building").asBoolean(true)) return;

            JsonNode payload = getArtifact(current);
            if (payload != null) {
                lastSeenBuild = current;
                handlePayload(payload);
            }
        } catch (Exception e) {
            System.err.println("Jenkins poll error: " + e.getMessage());
        }
    }

    private void handlePayload(JsonNode payload) {
        System.out.println("=== Новое GitLab событие ===");
        System.out.println("Тип:     " + payload.path("event_type").asText());
        System.out.println("Проект:  " + payload.path("project").asText());
        System.out.println("Из:      " + payload.path("source_branch").asText());
        System.out.println("В:       " + payload.path("target_branch").asText());
        System.out.println("Автор:   " + payload.path("user").asText());
    }

    private int getLastBuildNumber() throws Exception {
        String url = props.getUrl() + "/" + props.getJobPath()
                + "/api/json?tree=lastBuild[number]";
        JsonNode json = mapper.readTree(get(url));
        return json.path("lastBuild").path("number").asInt();
    }

    private JsonNode getBuildInfo(int number) throws Exception {
        String url = props.getUrl() + "/" + props.getJobPath()
                + "/" + number + "/api/json?tree=building,result";
        return mapper.readTree(get(url));
    }

    private JsonNode getArtifact(int number) throws Exception {
        String url = props.getUrl() + "/" + props.getJobPath()
                + "/" + number + "/artifact/payload.json";
        return mapper.readTree(get(url));
    }

    private String get(String url) throws Exception {
        String auth = Base64.getEncoder().encodeToString(
                (props.getUsername() + ":" + props.getToken()).getBytes()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> resp = rest.exchange(
                url, HttpMethod.GET, entity, String.class
        );
        return resp.getBody();
    }
}