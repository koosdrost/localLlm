package com.example.demoai.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class ToolsConfig {

    // Tool 1: Database zoeken
    @Bean
    @Description("Zoekt producten in de database op naam")
    public Function<SearchRequest, SearchResponse> searchProducts() {
        return request -> {
            System.out.println("🔍 Tool called: searchProducts - query: " + request.query());

            // Simuleer database call
            if (request.query().toLowerCase().contains("laptop")) {
                return new SearchResponse(
                        "Gevonden: Dell XPS 15 (€1499), MacBook Pro (€2399)"
                );
            }
            return new SearchResponse("Geen producten gevonden");
        };
    }

    // Tool 2: Prijs berekenen
    @Bean
    @Description("Berekent de totaalprijs inclusief BTW")
    public Function<PriceRequest, PriceResponse> calculatePrice() {
        return request -> {
            System.out.println("💰 Tool called: calculatePrice - amount: " + request.amount());

            double withTax = request.amount() * 1.21;
            return new PriceResponse(
                    request.amount(),
                    withTax,
                    "21% BTW toegepast"
            );
        };
    }

    // Tool 3: Email versturen (simulatie)
    @Bean
    @Description("Stuurt een email naar een klant")
    public Function<EmailRequest, EmailResponse> sendEmail() {
        return request -> {
            System.out.println("📧 Tool called: sendEmail - to: " + request.to());

            // Hier zou je echte email logica hebben
            return new EmailResponse(
                    true,
                    "Email verstuurd naar " + request.to()
            );
        };
    }

    // Records voor tool parameters
    public record SearchRequest(String query) {}
    public record SearchResponse(String results) {}

    public record PriceRequest(double amount) {}
    public record PriceResponse(double original, double withTax, String message) {}

    public record EmailRequest(String to, String subject, String body) {}
    public record EmailResponse(boolean success, String message) {}
}