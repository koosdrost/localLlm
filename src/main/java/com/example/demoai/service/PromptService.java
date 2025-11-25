package com.example.demoai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class PromptService {
    private static final Logger log = LoggerFactory.getLogger(PromptService.class);
    private final WebClient webClient;
    private String currentModel;

    public PromptService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        this.currentModel = "qwen iets"; // default model
    }

    public Mono<Map<String, Object>> getModelInfo() {
        return webClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> {
                    log.info("Received model info: {}", response);
                    // Auto-select first model if available
                    if (response.containsKey("models")) {
                        List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
                        if (!models.isEmpty()) {
                            String firstModel = (String) models.get(0).get("name");
                            if (firstModel != null && !firstModel.isEmpty()) {
                                setCurrentModel(firstModel);
                                log.info("Auto-selected model: {}", firstModel);
                            }
                        }
                    }
                })
                .doOnError(error -> log.error("Error fetching model info: {}", error.getMessage(), error));
    }

    public void setCurrentModel(String model) {
        this.currentModel = model;
        log.info("Current model set to: {}", model);
    }

    public String getCurrentModel() {
        return this.currentModel;
    }

    public Mono<String> generate(String prompt) {
        log.info("Generating {} response for prompt: {}", currentModel, prompt);

        Map<String, Object> request = Map.of(
                "model", currentModel,
                "prompt", prompt,
                "stream", false
        );

        log.debug("Request to Ollama: {}", request);

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> log.info("Received response from Ollama: {}", response))
                .map(response -> {
                    String aiResponse = (String) response.get("response");
                    log.info("Extracted AI response: {}", aiResponse);
                    return aiResponse;
                })
                .doOnError(error -> log.error("Error calling Ollama API: {}", error.getMessage(), error));
    }
}