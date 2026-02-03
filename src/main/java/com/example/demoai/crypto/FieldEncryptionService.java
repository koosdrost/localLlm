package com.example.demoai.crypto;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * High-level service for encrypting entity fields
 * <p>
 * Implements Option 3 from the JIO report:
 * - AES-GCM for encryption (non-deterministic, authenticated)
 * - HMAC for searchable indexes (deterministic, secure)
 * <p>
 * Usage pattern:
 * 1. Encrypt sensitive fields with encryptField()
 * 2. Create HMAC index with createSearchIndex()
 * 3. Search by HMAC using searchByIndex()
 * 4. Decrypt fields with decryptField()
 */
@Service
public class FieldEncryptionService {

    private final EncryptionService encryptionService;
    private final HmacService hmacService;
    private final KeyManagementService keyManagementService;

    public FieldEncryptionService(EncryptionService encryptionService,
                                  HmacService hmacService,
                                  KeyManagementService keyManagementService) {
        this.encryptionService = encryptionService;
        this.hmacService = hmacService;
        this.keyManagementService = keyManagementService;
    }

    /**
     * Encrypt a field value using the DEK for the specified table
     */
    public Mono<String> encryptField(String plaintext, String tableName) {
        if (plaintext == null) {
            return Mono.empty();
        }
        return keyManagementService.getDek(tableName)
                .map(dek -> encryptionService.encrypt(plaintext, dek));
    }

    /**
     * Decrypt a field value using the DEK for the specified table
     */
    public Mono<String> decryptField(String ciphertext, String tableName) {
        if (ciphertext == null) {
            return Mono.empty();
        }
        return keyManagementService.getDek(tableName)
                .map(dek -> encryptionService.decrypt(ciphertext, dek));
    }

    /**
     * Create HMAC search index for a field value
     */
    public Mono<String> createSearchIndex(String plaintext, String tableName) {
        if (plaintext == null) {
            return Mono.empty();
        }
        return keyManagementService.getHmacKey(tableName)
                .map(hmacKey -> hmacService.generateHmac(plaintext, hmacKey));
    }

    /**
     * Encrypt and create search index in one operation
     * Returns EncryptedField with both ciphertext and HMAC index
     */
    public Mono<EncryptedField> encryptWithIndex(String plaintext, String tableName) {
        if (plaintext == null) {
            return Mono.just(new EncryptedField(null, null));
        }

        return Mono.zip(
                encryptField(plaintext, tableName),
                createSearchIndex(plaintext, tableName)
        ).map(tuple -> new EncryptedField(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Generate search HMAC for querying
     * Use this to search for records by encrypted field
     */
    public Mono<String> generateSearchHmac(String searchValue, String tableName) {
        return createSearchIndex(searchValue, tableName);
    }

    /**
     * Result of encryption with search index
     */
    public record EncryptedField(String ciphertext, String hmacIndex) {
    }
}
