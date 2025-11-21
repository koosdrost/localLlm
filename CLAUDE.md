# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.0-RC2 reactive web application (JAR packaging) configured with Java 21. The project is named `demo-AI` and uses Maven as its build tool. It provides a REST API for interacting with local Ollama LLM models.

## Build and Development Commands

### Build the project
```bash
./mvnw clean package
```

### Run the application
```bash
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

### Run a single test class
```bash
./mvnw test -Dtest=DemoAiApplicationTests
```

### Package as JAR file
```bash
./mvnw clean package
```
The JAR file will be created in `target/demo-AI-0.0.1-SNAPSHOT.jar`

### Run the packaged JAR
```bash
java -jar target/demo-AI-0.0.1-SNAPSHOT.jar
```

## Architecture

### Technology Stack
- **Framework**: Spring Boot 4.0.0-RC2 (based on Spring Framework 7.x and Jakarta EE 11)
- **Web Framework**: Spring WebFlux (reactive, non-blocking)
- **Server**: Netty (embedded reactive server)
- **Java Version**: 21
- **Build Tool**: Maven
- **Packaging**: JAR (standalone executable)
- **Dependencies**:
  - `spring-boot-starter-webflux` - Reactive web applications with WebFlux and Netty
  - `spring-boot-devtools` - Development-time features (hot reload, etc.)
  - `reactor-test` - Testing support for reactive streams

### Application Structure
- **Main class**: `com.example.demoai.DemoAiApplication` - Standard Spring Boot application entry point
- **Base package**: `com.example.demoai`
- **Controllers**:
  - `AiController` - REST API endpoints for AI generation (`/api/ai/*`)
- **Services**:
  - `AiService` - Reactive service for Ollama LLM integration using WebClient
- **Frontend**: Static HTML interface in `src/main/resources/static/index.html`

### Configuration
- Application properties are in `src/main/resources/application.properties`
- Currently configured with application name: `demo-AI`
- Ollama endpoint: `http://localhost:11434`
- Default model: `qwen2.5:3b`

### API Endpoints
- **POST** `/api/ai/generate` - Generate AI responses from prompts
  - Request body: `{"prompt": "your question here"}`
  - Response: AI-generated text
- **GET** `/api/ai/health` - Health check endpoint
  - Response: `{"status": "UP", "service": "AI Service"}`
- **GET** `/` - Web interface for interacting with the AI

### Deployment Model
This is a fully reactive application using:
- **WebFlux** for non-blocking request handling
- **Netty** as the embedded server (instead of Tomcat)
- **WebClient** for reactive HTTP calls to Ollama
- **Reactor** for reactive stream processing

The application runs as a standalone JAR with embedded Netty server.
