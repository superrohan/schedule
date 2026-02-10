package com.enterprise.scheduler.client;

import com.enterprise.scheduler.security.ServiceTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ControllerClient {

    private static final Logger log = LoggerFactory.getLogger(ControllerClient.class);

    private final WebClient webClient;
    private final ServiceTokenProvider tokenProvider;

    public ControllerClient(
            @Value("${controller-app.base-url}") String baseUrl,
            ServiceTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("ControllerClient initialized with base URL: {}", baseUrl);
    }

    public void launchScan(String scanId) {
        String correlationId = MDC.get("correlationId");
        log.info("Launching scan '{}' via ControllerApp", scanId);

        String token = tokenProvider.getAccessToken();

        webClient.post()
                .uri("/controller/internal/scheduler/launch-scan/{scanId}", scanId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Correlation-Id", correlationId != null ? correlationId : "")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("ControllerApp returned error status {} for scan '{}'",
                            response.statusCode(), scanId);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "ControllerApp error " + response.statusCode() + ": " + body)));
                })
                .bodyToMono(String.class)
                .doOnSuccess(body -> log.info("Scan '{}' launched successfully", scanId))
                .block();
    }
}
