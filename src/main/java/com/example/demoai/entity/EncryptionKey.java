package com.example.demoai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entity for storing encrypted DEKs in the database
 * <p>
 * From the JIO report: DEKs are stored in the database, encrypted with the KEK.
 * This allows for:
 * - Multiple DEKs per table/context
 * - Key versioning for rotation
 * - Audit trail of key lifecycle
 */
@Table("encryption_keys")
public class EncryptionKey {

    @Id
    private Long id;

    private String tableName;      // Which table/context this key is for
    private String keyType;        // "DEK" or "HMAC"
    private String encryptedKey;   // The key encrypted with KEK (Base64)
    private boolean active;        // Is this the current active key?
    private int version;           // Key version for rotation tracking
    private LocalDateTime createdAt;
    private LocalDateTime rotatedAt;

    public EncryptionKey() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRotatedAt() {
        return rotatedAt;
    }

    public void setRotatedAt(LocalDateTime rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}
