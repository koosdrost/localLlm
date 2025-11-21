package com.example.demoai.controller;

import com.example.demoai.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generate(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");

        if (prompt == null || prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt is required"));
        }

        return aiService.generate(prompt)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError()
                                .body("Error generating response: " + e.getMessage())
                ));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Service"
        )));
    }

    @GetMapping("/models")
    public Mono<ResponseEntity<Map<String, Object>>> getModels() {
        return aiService.getModelInfo()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError()
                                .body(Map.of("error", "Error fetching models: " + e.getMessage()))
                ));
    }
}
