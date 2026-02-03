# demo-AI

A Spring Boot 4.0 reactive web application demonstrating integration with local Ollama LLM models and field-level
encryption using AES-GCM with HMAC indexing.

## Features

- **AI Generation**: REST API for interacting with local Ollama LLM models
- **Prompt History**: Persistent storage of prompts and responses
- **Field-Level Encryption**: AES-GCM-256 encryption with HMAC for searchable encrypted data
- **Envelope Encryption**: DEK/KEK key management pattern
- **Reactive Architecture**: Built with Spring WebFlux and R2DBC

## Prerequisites

- Java 21
- Maven 3.8+
- Ollama running locally on port 11434 (for AI features)

## Quick Start

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

## API Endpoints

### AI Endpoints (`/api/ai`)

| Method | Endpoint           | Description                      |
|--------|--------------------|----------------------------------|
| POST   | `/api/ai/generate` | Generate AI response from prompt |
| GET    | `/api/ai/health`   | Health check                     |
| GET    | `/api/ai/models`   | List available Ollama models     |
| GET    | `/api/ai/history`  | Get prompt history               |

### Crypto Endpoints (`/api/crypto`)

| Method | Endpoint                                | Description                               |
|--------|-----------------------------------------|-------------------------------------------|
| POST   | `/api/crypto/demo/aes-gcm`              | Demo AES-GCM non-deterministic encryption |
| POST   | `/api/crypto/demo/hmac`                 | Demo deterministic HMAC                   |
| POST   | `/api/crypto/secure-data`               | Store encrypted data                      |
| GET    | `/api/crypto/secure-data`               | Get all data (decrypted)                  |
| GET    | `/api/crypto/secure-data/raw`           | Get raw encrypted data                    |
| GET    | `/api/crypto/secure-data/search?value=` | Search by HMAC index                      |
| GET    | `/api/crypto/keys/info`                 | Key info                                  |
| POST   | `/api/crypto/keys/rotate/{table}`       | Rotate DEK                                |
| POST   | `/api/crypto/encrypt`                   | Encrypt a field                           |
| POST   | `/api/crypto/decrypt`                   | Decrypt a field                           |

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database (H2 in-memory by default)
spring.r2dbc.url=r2dbc:h2:mem:///demoai

# PostgreSQL (for production)
#spring.r2dbc.url=r2dbc:postgresql://localhost:5432/demoai
#spring.r2dbc.username=postgres
#spring.r2dbc.password=postgres

# Encryption KEK (Base64 encoded AES-256 key)
# In production: load from keystore or HSM
#encryption.kek=YOUR_BASE64_KEY
```

## Technology Stack

- Spring Boot 4.0.0
- Spring WebFlux (reactive web)
- Spring Data R2DBC (reactive database)
- Netty (embedded server)
- H2 / PostgreSQL (database)
- AES-GCM-256 / HMAC-SHA256 (encryption)

## License

MIT
