package com.example.demoai.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit logging for all cryptographic operations
 * <p>
 * From the JIO report: Add audit logging for all cryptographic operations.
 * This provides traceability and helps detect potential security incidents.
 * <p>
 * IMPORTANT: Never log actual keys, plaintext, or sensitive data!
 */
@Service
public class CryptoAuditService {
    private static final Logger auditLog = LoggerFactory.getLogger("CRYPTO_AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void logEncryption(String algorithm, int dataSize) {
        log("ENCRYPT", String.format("algorithm=%s, dataSize=%d bytes", algorithm, dataSize));
    }

    public void logDecryption(String algorithm, int dataSize) {
        log("DECRYPT", String.format("algorithm=%s, dataSize=%d bytes", algorithm, dataSize));
    }

    public void logKeyGeneration(String keyType) {
        log("KEY_GEN", String.format("keyType=%s", keyType));
    }

    public void logKeyEncryption(String keyType) {
        log("KEY_ENCRYPT", String.format("keyType=%s (envelope encryption)", keyType));
    }

    public void logKeyDecryption(String keyType) {
        log("KEY_DECRYPT", String.format("keyType=%s (envelope decryption)", keyType));
    }

    public void logKeyRotation(String keyType, String keyId) {
        log("KEY_ROTATE", String.format("keyType=%s, keyId=%s", keyType, keyId));
    }

    public void logHmacGeneration() {
        log("HMAC_GEN", "algorithm=HMAC-SHA256");
    }

    public void logHmacVerification(boolean success) {
        log("HMAC_VERIFY", String.format("result=%s", success ? "MATCH" : "NO_MATCH"));
    }

    public void logError(String operation, String errorMessage) {
        log("ERROR", String.format("operation=%s, error=%s", operation, sanitize(errorMessage)));
    }

    public void logDekCreation(String dekId, String tableName) {
        log("DEK_CREATE", String.format("dekId=%s, table=%s", dekId, tableName));
    }

    public void logDekRotation(String oldDekId, String newDekId, int recordsReEncrypted) {
        log("DEK_ROTATE", String.format("oldDekId=%s, newDekId=%s, recordsReEncrypted=%d",
                oldDekId, newDekId, recordsReEncrypted));
    }

    private void log(String operation, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String threadName = Thread.currentThread().getName();
        auditLog.info("[{}] [{}] [{}] {}", timestamp, operation, threadName, details);
    }

    /**
     * Sanitize error messages to prevent log injection
     */
    private String sanitize(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n]", " ").substring(0, Math.min(input.length(), 200));
    }
}
