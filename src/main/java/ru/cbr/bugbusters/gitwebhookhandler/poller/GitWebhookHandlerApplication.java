package ru.cbr.bugbusters.gitwebhookhandler.poller;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GitWebhookHandlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitWebhookHandlerApplication.class, args);
    }
}