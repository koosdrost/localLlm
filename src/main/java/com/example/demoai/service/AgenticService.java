package com.example.demoai.service;

import org.springframework.stereotype.Service;

@Service
public class AgenticService {
//
//    private final ChatClient chatClient;
//
//    public AgenticService(ChatClient.Builder chatClientBuilder) {
//        // ⚠️ BELANGRIJK: Configureer ChatClient met tools
//        this.chatClient = chatClientBuilder
//                .defaultFunctions(
//                        "searchProducts",      // Namen moeten matchen met @Bean namen
//                        "calculatePrice",
//                        "sendEmail"
//                )
//                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
//                )
//                .build();
//    }
//
//    public String processAgenticRequest(String userMessage) {
//        System.out.println("\n🤖 User: " + userMessage);
//        System.out.println("⏳ Agent denkt na en gebruikt tools indien nodig...\n");
//
//        // Spring AI regelt automatisch:
//        // 1. LLM beslist welke tools nodig zijn
//        // 2. Tools worden uitgevoerd
//        // 3. Resultaten gaan terug naar LLM
//        // 4. LLM geeft finaal antwoord
//
//        String response = chatClient.prompt()
//                .user(userMessage)
//                .call()
//                .content();
//
//        System.out.println("\n✅ Agent: " + response);
//        return response;
//    }
}