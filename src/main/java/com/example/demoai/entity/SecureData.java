package com.example.demoai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Example entity demonstrating encrypted storage
 * <p>
 * Pattern from the JIO report (Option 3: AES-GCM + HMAC):
 * - sensitiveData: Encrypted with AES-GCM (non-deterministic, can't search directly)
 * - sensitiveDataHmac: HMAC index for exact-match searches
 * <p>
 * Database sees only encrypted blobs and HMAC hashes.
 * Even a DBA with full database access cannot read the sensitive data.
 */
@Table("secure_data")
public class SecureData {

    @Id
    private Long id;

    // Encrypted field (AES-GCM) - non-deterministic
    private String sensitiveData;

    // HMAC index for searching - deterministic but secure
    private String sensitiveDataHmac;

    // Non-sensitive metadata (not encrypted)
    private String category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SecureData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(String sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    public String getSensitiveDataHmac() {
        return sensitiveDataHmac;
    }

    public void setSensitiveDataHmac(String sensitiveDataHmac) {
        this.sensitiveDataHmac = sensitiveDataHmac;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
