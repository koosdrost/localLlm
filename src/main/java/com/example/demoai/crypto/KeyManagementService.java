package com.example.demoai.crypto;

import com.example.demoai.entity.EncryptionKey;
import com.example.demoai.repository.EncryptionKeyRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Key Management Service implementing Envelope Encryption
 * <p>
 * From the JIO report:
 * - DEK (Data Encryption Key): Encrypts the application data. Multiple DEKs possible (per table).
 * - KEK (Key Encryption Key): Encrypts the DEKs. Stored in secrets manager.
 * - On KEK rotation: only re-encrypt DEKs (not the data)
 * - On DEK rotation: re-encrypt data incrementally per table
 * <p>
 * This implementation:
 * - KEK loaded from configuration (in production: from Elytron/Vault/HSM)
 * - DEKs stored in database, encrypted with KEK
 * - Separate HMAC key per table for searchable indexes
 * - Support for key rotation
 */
@Service
public class KeyManagementService {
    private static final Logger log = LoggerFactory.getLogger(KeyManagementService.class);

    private final EncryptionService encryptionService;
    private final EncryptionKeyRepository keyRepository;
    private final CryptoAuditService auditService;
    // Cache for decrypted DEKs (in memory only!)
    private final Map<String, byte[]> dekCache = new ConcurrentHashMap<>();
    private final Map<String, byte[]> hmacKeyCache = new ConcurrentHashMap<>();
    // KEK loaded from configuration (in production: secrets manager)
    @Value("${encryption.kek:#{null}}")
    private String kekBase64;
    private byte[] kek;

    public KeyManagementService(EncryptionService encryptionService,
                                EncryptionKeyRepository keyRepository,
                                CryptoAuditService auditService) {
        this.encryptionService = encryptionService;
        this.keyRepository = keyRepository;
        this.auditService = auditService;
    }

    @PostConstruct
    public void init() {
        if (kekBase64 != null && !kekBase64.isEmpty()) {
            this.kek = Base64.getDecoder().decode(kekBase64);
            log.info("KEK loaded from configuration");
        } else {
            // Generate a temporary KEK for development (NOT for production!)
            log.warn("No KEK configured - generating temporary KEK. DO NOT USE IN PRODUCTION!");
            this.kek = encryptionService.generateKey();
            log.info("Development KEK (Base64): {}", Base64.getEncoder().encodeToString(kek));
        }
    }

    /**
     * Get or create DEK for a specific table/context
     */
    public Mono<byte[]> getDek(String tableName) {
        // Check cache first
        byte[] cachedDek = dekCache.get(tableName);
        if (cachedDek != null) {
            return Mono.just(cachedDek);
        }

        // Load from database or create new
        return keyRepository.findByTableNameAndKeyTypeAndActiveTrue(tableName, "DEK")
                .map(key -> {
                    byte[] dek = encryptionService.decryptKey(key.getEncryptedKey(), kek);
                    dekCache.put(tableName, dek);
                    return dek;
                })
                .switchIfEmpty(createDek(tableName));
    }

    /**
     * Get or create HMAC key for a specific table/context
     */
    public Mono<byte[]> getHmacKey(String tableName) {
        // Check cache first
        byte[] cachedKey = hmacKeyCache.get(tableName);
        if (cachedKey != null) {
            return Mono.just(cachedKey);
        }

        // Load from database or create new
        return keyRepository.findByTableNameAndKeyTypeAndActiveTrue(tableName, "HMAC")
                .map(key -> {
                    byte[] hmacKey = encryptionService.decryptKey(key.getEncryptedKey(), kek);
                    hmacKeyCache.put(tableName, hmacKey);
                    return hmacKey;
                })
                .switchIfEmpty(createHmacKey(tableName));
    }

    /**
     * Create a new DEK for a table
     */
    private Mono<byte[]> createDek(String tableName) {
        byte[] dek = encryptionService.generateKey();
        String encryptedDek = encryptionService.encryptKey(dek, kek);

        EncryptionKey key = new EncryptionKey();
        key.setTableName(tableName);
        key.setKeyType("DEK");
        key.setEncryptedKey(encryptedDek);
        key.setActive(true);
        key.setCreatedAt(LocalDateTime.now());
        key.setVersion(1);

        return keyRepository.save(key)
                .doOnSuccess(saved -> {
                    dekCache.put(tableName, dek);
                    auditService.logDekCreation(saved.getId().toString(), tableName);
                    log.info("Created new DEK for table: {}", tableName);
                })
                .thenReturn(dek);
    }

    /**
     * Create a new HMAC key for a table
     */
    private Mono<byte[]> createHmacKey(String tableName) {
        byte[] hmacKey = encryptionService.generateKey();
        String encryptedKey = encryptionService.encryptKey(hmacKey, kek);

        EncryptionKey key = new EncryptionKey();
        key.setTableName(tableName);
        key.setKeyType("HMAC");
        key.setEncryptedKey(encryptedKey);
        key.setActive(true);
        key.setCreatedAt(LocalDateTime.now());
        key.setVersion(1);

        return keyRepository.save(key)
                .doOnSuccess(saved -> {
                    hmacKeyCache.put(tableName, hmacKey);
                    log.info("Created new HMAC key for table: {}", tableName);
                })
                .thenReturn(hmacKey);
    }

    /**
     * Rotate DEK for a table
     * Returns both old and new DEK for re-encryption process
     */
    public Mono<DekRotationResult> rotateDek(String tableName) {
        return keyRepository.findByTableNameAndKeyTypeAndActiveTrue(tableName, "DEK")
                .flatMap(oldKey -> {
                    byte[] oldDek = encryptionService.decryptKey(oldKey.getEncryptedKey(), kek);
                    byte[] newDek = encryptionService.generateKey();
                    String encryptedNewDek = encryptionService.encryptKey(newDek, kek);

                    // Deactivate old key
                    oldKey.setActive(false);

                    // Create new key
                    EncryptionKey newKey = new EncryptionKey();
                    newKey.setTableName(tableName);
                    newKey.setKeyType("DEK");
                    newKey.setEncryptedKey(encryptedNewDek);
                    newKey.setActive(true);
                    newKey.setCreatedAt(LocalDateTime.now());
                    newKey.setVersion(oldKey.getVersion() + 1);

                    return keyRepository.save(oldKey)
                            .then(keyRepository.save(newKey))
                            .doOnSuccess(saved -> {
                                dekCache.put(tableName, newDek);
                                auditService.logKeyRotation("DEK", saved.getId().toString());
                            })
                            .thenReturn(new DekRotationResult(oldDek, newDek));
                });
    }

    /**
     * Clear key caches (call after KEK rotation)
     */
    public void clearCaches() {
        dekCache.clear();
        hmacKeyCache.clear();
        log.info("Cleared all key caches");
    }

    /**
     * Result of DEK rotation containing both old and new keys
     */
    public record DekRotationResult(byte[] oldDek, byte[] newDek) {
    }
}
