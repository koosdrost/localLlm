package com.example.demoai.service;

import com.example.demoai.crypto.FieldEncryptionService;
import com.example.demoai.entity.SecureData;
import com.example.demoai.repository.SecureDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service demonstrating secure data storage with encryption
 * <p>
 * Implements Option 3 from the JIO report:
 * - Store: Encrypt with AES-GCM + create HMAC index
 * - Search: Generate HMAC and query by index
 * - Retrieve: Decrypt with AES-GCM
 */
@Service
public class SecureDataService {
    private static final Logger log = LoggerFactory.getLogger(SecureDataService.class);
    private static final String TABLE_NAME = "secure_data";

    private final SecureDataRepository repository;
    private final FieldEncryptionService fieldEncryption;

    public SecureDataService(SecureDataRepository repository,
                             FieldEncryptionService fieldEncryption) {
        this.repository = repository;
        this.fieldEncryption = fieldEncryption;
    }

    /**
     * Store sensitive data with encryption
     */
    public Mono<SecureData> store(String sensitiveData, String category) {
        log.info("Storing encrypted data in category: {}", category);

        return fieldEncryption.encryptWithIndex(sensitiveData, TABLE_NAME)
                .flatMap(encrypted -> {
                    SecureData entity = new SecureData();
                    entity.setSensitiveData(encrypted.ciphertext());
                    entity.setSensitiveDataHmac(encrypted.hmacIndex());
                    entity.setCategory(category);
                    entity.setCreatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .doOnSuccess(saved -> log.info("Stored encrypted data with id: {}", saved.getId()));
    }

    /**
     * Search for data by exact match on sensitive field
     * Uses HMAC index for secure searching
     */
    public Flux<SecureDataDTO> findBySensitiveData(String searchValue) {
        log.info("Searching for data by encrypted field");

        return fieldEncryption.generateSearchHmac(searchValue, TABLE_NAME)
                .flatMapMany(repository::findAllBySensitiveDataHmac)
                .flatMap(this::toDTO);
    }

    /**
     * Get data by ID and decrypt
     */
    public Mono<SecureDataDTO> findById(Long id) {
        return repository.findById(id)
                .flatMap(this::toDTO);
    }

    /**
     * Get all data (decrypted)
     */
    public Flux<SecureDataDTO> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .flatMap(this::toDTO);
    }

    /**
     * Get all data by category (decrypted)
     */
    public Flux<SecureDataDTO> findByCategory(String category) {
        return repository.findByCategory(category)
                .flatMap(this::toDTO);
    }

    /**
     * Get raw encrypted data (for demonstrating what's actually stored)
     */
    public Flux<SecureData> findAllRaw() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Convert entity to DTO with decryption
     */
    private Mono<SecureDataDTO> toDTO(SecureData entity) {
        return fieldEncryption.decryptField(entity.getSensitiveData(), TABLE_NAME)
                .map(decrypted -> new SecureDataDTO(
                        entity.getId(),
                        decrypted,
                        entity.getCategory(),
                        entity.getCreatedAt()
                ));
    }

    /**
     * DTO with decrypted sensitive data
     */
    public record SecureDataDTO(
            Long id,
            String sensitiveData,
            String category,
            LocalDateTime createdAt
    ) {
    }
}
