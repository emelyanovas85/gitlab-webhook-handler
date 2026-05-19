package ru.cbr.bugbusters.gitwebhookhandler.poller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jenkins")
@Component
@Getter
@Setter
public class JenkinsProperties {
    private String url;
    private String jobPath;
    private String username;
    private String token;
    private int pollIntervalSeconds;
    // getters + setters
}
