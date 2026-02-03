package com.example.demoai.controller;

import com.example.demoai.crypto.EncryptionService;
import com.example.demoai.crypto.FieldEncryptionService;
import com.example.demoai.crypto.HmacService;
import com.example.demoai.crypto.KeyManagementService;
import com.example.demoai.entity.SecureData;
import com.example.demoai.service.SecureDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Demo controller for encryption functionality
 * <p>
 * Demonstrates all concepts from the JIO report:
 * - AES-GCM encryption (non-deterministic)
 * - HMAC for searchable indexes
 * - Envelope encryption (DEK/KEK)
 * - Secure storage and retrieval
 */
@RestController
@RequestMapping("/api/crypto")
public class EncryptionDemoController {

    private final EncryptionService encryptionService;
    private final HmacService hmacService;
    private final KeyManagementService keyManagementService;
    private final FieldEncryptionService fieldEncryptionService;
    private final SecureDataService secureDataService;

    public EncryptionDemoController(EncryptionService encryptionService,
                                    HmacService hmacService,
                                    KeyManagementService keyManagementService,
                                    FieldEncryptionService fieldEncryptionService,
                                    SecureDataService secureDataService) {
        this.encryptionService = encryptionService;
        this.hmacService = hmacService;
        this.keyManagementService = keyManagementService;
        this.fieldEncryptionService = fieldEncryptionService;
        this.secureDataService = secureDataService;
    }

    // ==================== BASIC ENCRYPTION DEMO ====================

    /**
     * Demo: AES-GCM encryption is non-deterministic
     * Same plaintext produces different ciphertext each time
     */
    @PostMapping("/demo/aes-gcm")
    public Mono<ResponseEntity<Map<String, Object>>> demoAesGcm(@RequestBody Map<String, String> request) {
        String plaintext = request.get("plaintext");
        byte[] key = encryptionService.generateKey();

        // Encrypt the same plaintext twice
        String ciphertext1 = encryptionService.encrypt(plaintext, key);
        String ciphertext2 = encryptionService.encrypt(plaintext, key);

        // Decrypt both
        String decrypted1 = encryptionService.decrypt(ciphertext1, key);
        String decrypted2 = encryptionService.decrypt(ciphertext2, key);

        return Mono.just(ResponseEntity.ok(Map.of(
                "plaintext", plaintext,
                "ciphertext1", ciphertext1,
                "ciphertext2", ciphertext2,
                "ciphertextsEqual", ciphertext1.equals(ciphertext2),
                "decrypted1", decrypted1,
                "decrypted2", decrypted2,
                "explanation", "AES-GCM is non-deterministic: same plaintext produces different ciphertext due to random IV. This prevents frequency analysis attacks."
        )));
    }

    /**
     * Demo: HMAC is deterministic (for searching)
     */
    @PostMapping("/demo/hmac")
    public Mono<ResponseEntity<Map<String, Object>>> demoHmac(@RequestBody Map<String, String> request) {
        String value = request.get("value");
        byte[] key = encryptionService.generateKey();

        // Generate HMAC twice
        String hmac1 = hmacService.generateHmac(value, key);
        String hmac2 = hmacService.generateHmac(value, key);

        return Mono.just(ResponseEntity.ok(Map.of(
                "value", value,
                "hmac1", hmac1,
                "hmac2", hmac2,
                "hmacsEqual", hmac1.equals(hmac2),
                "explanation", "HMAC is deterministic: same value always produces same HMAC. This allows exact-match searching without exposing the plaintext."
        )));
    }

    // ==================== SECURE DATA STORAGE ====================

    /**
     * Store sensitive data with encryption and HMAC index
     */
    @PostMapping("/secure-data")
    public Mono<ResponseEntity<Map<String, Object>>> storeSecureData(@RequestBody Map<String, String> request) {
        String sensitiveData = request.get("sensitiveData");
        String category = request.getOrDefault("category", "default");

        return secureDataService.store(sensitiveData, category)
                .map(saved -> ResponseEntity.ok(Map.of(
                        "id", saved.getId(),
                        "category", category,
                        "message", "Data encrypted and stored successfully",
                        "encryptedValue", saved.getSensitiveData(),
                        "hmacIndex", saved.getSensitiveDataHmac()
                )));
    }

    /**
     * Search by sensitive data (uses HMAC for secure searching)
     */
    @GetMapping("/secure-data/search")
    public Flux<SecureDataService.SecureDataDTO> searchSecureData(@RequestParam String value) {
        return secureDataService.findBySensitiveData(value);
    }

    /**
     * Get all stored data (decrypted)
     */
    @GetMapping("/secure-data")
    public Flux<SecureDataService.SecureDataDTO> getAllSecureData() {
        return secureDataService.findAll();
    }

    /**
     * Get raw database view (shows encrypted data as stored)
     */
    @GetMapping("/secure-data/raw")
    public Flux<SecureData> getAllSecureDataRaw() {
        return secureDataService.findAllRaw();
    }

    /**
     * Get secure data by ID (decrypted)
     */
    @GetMapping("/secure-data/{id}")
    public Mono<ResponseEntity<SecureDataService.SecureDataDTO>> getSecureDataById(@PathVariable Long id) {
        return secureDataService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ==================== KEY MANAGEMENT ====================

    /**
     * Demo: Envelope encryption - show key structure
     */
    @GetMapping("/keys/info")
    public Mono<ResponseEntity<Map<String, Object>>> getKeyInfo() {
        return Mono.zip(
                keyManagementService.getDek("secure_data"),
                keyManagementService.getHmacKey("secure_data")
        ).map(tuple -> ResponseEntity.ok(Map.of(
                "dekLength", tuple.getT1().length + " bytes (256 bits)",
                "hmacKeyLength", tuple.getT2().length + " bytes (256 bits)",
                "algorithm", "AES-256-GCM for encryption, HMAC-SHA256 for indexing",
                "note", "Keys are stored encrypted with KEK (envelope encryption)"
        )));
    }

    /**
     * Rotate DEK (demonstrates key rotation)
     */
    @PostMapping("/keys/rotate/{tableName}")
    public Mono<ResponseEntity<Map<String, String>>> rotateDek(@PathVariable String tableName) {
        return keyManagementService.rotateDek(tableName)
                .map(result -> ResponseEntity.ok(Map.of(
                        "status", "success",
                        "tableName", tableName,
                        "message", "DEK rotated. Old data should be re-encrypted with new key.",
                        "note", "In production, run batch job to re-encrypt existing records"
                )));
    }

    // ==================== FIELD-LEVEL ENCRYPTION ====================

    /**
     * Encrypt a single field
     */
    @PostMapping("/encrypt")
    public Mono<ResponseEntity<Map<String, String>>> encryptField(@RequestBody Map<String, String> request) {
        String plaintext = request.get("plaintext");
        String tableName = request.getOrDefault("tableName", "default");

        return fieldEncryptionService.encryptWithIndex(plaintext, tableName)
                .map(result -> ResponseEntity.ok(Map.of(
                        "ciphertext", result.ciphertext(),
                        "hmacIndex", result.hmacIndex()
                )));
    }

    /**
     * Decrypt a single field
     */
    @PostMapping("/decrypt")
    public Mono<ResponseEntity<Map<String, String>>> decryptField(@RequestBody Map<String, String> request) {
        String ciphertext = request.get("ciphertext");
        String tableName = request.getOrDefault("tableName", "default");

        return fieldEncryptionService.decryptField(ciphertext, tableName)
                .map(plaintext -> ResponseEntity.ok(Map.of(
                        "plaintext", plaintext
                )));
    }
}
