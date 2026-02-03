package com.example.demoai.controller;

import com.example.demoai.service.AgenticService;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/agent")
public class AgentController {

    private final AgenticService agenticService;

    public AgentController(AgenticService agenticService) {
        this.agenticService = agenticService;
    }
//
//    @PostMapping("/chat")
//    public ChatResponse chat(@RequestBody ChatRequest request) {
//        String response = agenticService.processAgenticRequest(request.message());
//        return new ChatResponse(response);
//    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String response) {}
}