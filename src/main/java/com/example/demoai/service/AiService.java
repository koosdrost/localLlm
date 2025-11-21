package com.example.demoai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final WebClient webClient;

    public AiService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        log.info("AiService initialized with Ollama base URL: http://localhost:11434");
    }

    public Mono<String> generate(String prompt) {
        log.info("Generating response for prompt: {}", prompt);

        Map<String, Object> request = Map.of(
                "model", "qwen2.5:3b",
                "prompt", prompt,
                "stream", false
        );

        log.debug("Request to Ollama: {}", request);

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> log.info("Received response from Ollama: {}", response))
                .map(response -> {
                    String aiResponse = (String) response.get("response");
                    log.info("Extracted AI response: {}", aiResponse);
                    return aiResponse;
                })
                .doOnError(error -> log.error("Error calling Ollama API: {}", error.getMessage(), error));
    }
}