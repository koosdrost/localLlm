# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.0 reactive web application (JAR packaging) configured with Java 21. The project is named
`demo-AI` and uses Maven as its build tool. It provides:

- REST API for interacting with local Ollama LLM models
- Field-level encryption demo using AES-GCM with HMAC indexing
- Reactive database access with R2DBC

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

- **Framework**: Spring Boot 4.0.0 (based on Spring Framework 7.x and Jakarta EE 11)
- **Web Framework**: Spring WebFlux (reactive, non-blocking)
- **Database**: Spring Data R2DBC (reactive) with H2 (dev) / PostgreSQL (prod)
- **Server**: Netty (embedded reactive server)
- **Java Version**: 21
- **Build Tool**: Maven
- **Packaging**: JAR (standalone executable)
- **Dependencies**:
  - `spring-boot-starter-webflux` - Reactive web applications with WebFlux and Netty
  - `spring-boot-starter-data-r2dbc` - Reactive database access
  - `r2dbc-h2` / `r2dbc-postgresql` - Database drivers
  - `spring-boot-devtools` - Development-time features (hot reload, etc.)
  - `reactor-test` - Testing support for reactive streams

### Application Structure
- **Main class**: `com.example.demoai.DemoAiApplication` - Standard Spring Boot application entry point
- **Base package**: `com.example.demoai`
- **Controllers**:
  - `PromptController` - REST API endpoints for AI generation (`/api/ai/*`)
  - `EncryptionDemoController` - Encryption demo endpoints (`/api/crypto/*`)
  - `AgentController` - Agent endpoints (work in progress)
- **Services**:
  - `PromptService` - Reactive service for Ollama LLM integration using WebClient
  - `SecureDataService` - Encrypted data storage and retrieval
  - `EncryptionService` - AES-GCM-256 encryption/decryption
  - `HmacService` - HMAC-SHA256 for searchable indexes
  - `KeyManagementService` - DEK/KEK envelope encryption
  - `FieldEncryptionService` - High-level field encryption API
  - `CryptoAuditService` - Crypto operation logging
- **Entities**: `PromptHistory`, `SecureData`, `EncryptionKey`
- **Repositories**: R2DBC repositories for reactive database access
- **Frontend**: Static HTML interface in `src/main/resources/static/index.html`

### Configuration
- Application properties are in `src/main/resources/application.properties`
- Database schema in `src/main/resources/schema.sql`
- Currently configured with:
  - Application name: `demo-AI`
  - Ollama endpoint: `http://localhost:11434` (required only for AI features)
  - Database: H2 in-memory (default), PostgreSQL supported
  - KEK: Generated at startup (configure `encryption.kek` for production)

### API Endpoints

#### AI Endpoints (`/api/ai`) - Requires Ollama
- **POST** `/api/ai/generate` - Generate AI responses from prompts
  - Request body: `{"prompt": "your question here"}`
  - Response: AI-generated text
- **GET** `/api/ai/health` - Health check endpoint
- **GET** `/api/ai/models` - List available Ollama models
- **GET** `/api/ai/history` - Get prompt/response history

#### Crypto Endpoints (`/api/crypto`) - No Ollama required

- **POST** `/api/crypto/demo/aes-gcm` - Demo non-deterministic AES-GCM encryption
- **POST** `/api/crypto/demo/hmac` - Demo deterministic HMAC
- **POST** `/api/crypto/secure-data` - Store encrypted data
- **GET** `/api/crypto/secure-data` - Get all data (decrypted)
- **GET** `/api/crypto/secure-data/raw` - Get raw encrypted data
- **GET** `/api/crypto/secure-data/search` - Search by HMAC index
- **GET** `/api/crypto/secure-data/{id}` - Get by ID (decrypted)
- **GET** `/api/crypto/keys/info` - Key information
- **POST** `/api/crypto/keys/rotate/{tableName}` - Rotate DEK
- **POST** `/api/crypto/encrypt` - Encrypt a field
- **POST** `/api/crypto/decrypt` - Decrypt a field

### Deployment Model
This is a fully reactive application using:
- **WebFlux** for non-blocking request handling
- **Netty** as the embedded server (instead of Tomcat)
- **R2DBC** for reactive database access
- **WebClient** for reactive HTTP calls to Ollama
- **Reactor** for reactive stream processing

The application runs as a standalone JAR with embedded Netty server. The crypto functionality works independently
without Ollama.
