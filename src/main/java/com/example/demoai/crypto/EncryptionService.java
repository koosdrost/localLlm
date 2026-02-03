package com.example.demoai.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM-256 Encryption Service
 * <p>
 * Implements Option 3 from the JIO report: AES-GCM for confidentiality and integrity (AEAD).
 * - Non-deterministic: same plaintext produces different ciphertext each time
 * - Authenticated: includes integrity check (no tampering possible)
 * - Random IV for each encryption operation
 */
@Service
public class EncryptionService {
    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag
    private static final int AES_KEY_SIZE = 256; // AES-256

    private final SecureRandom secureRandom = new SecureRandom();
    private final CryptoAuditService auditService;

    public EncryptionService(CryptoAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Encrypt plaintext using AES-GCM-256
     * Format: [IV (12 bytes)][Ciphertext + Auth Tag]
     */
    public String encrypt(String plaintext, byte[] key) {
        if (plaintext == null) {
            return null;
        }

        try {
            // Generate random IV for each encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            String result = Base64.getEncoder().encodeToString(byteBuffer.array());

            auditService.logEncryption("AES-GCM-256", plaintext.length());
            log.debug("Encrypted {} bytes of data", plaintext.length());

            return result;
        } catch (Exception e) {
            auditService.logError("ENCRYPT", e.getMessage());
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * Decrypt ciphertext using AES-GCM-256
     * Verifies integrity via authentication tag
     */
    public String decrypt(String ciphertext, byte[] key) {
        if (ciphertext == null) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);

            // Extract IV from the beginning
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintext = cipher.doFinal(encryptedData);

            auditService.logDecryption("AES-GCM-256", plaintext.length);
            log.debug("Decrypted {} bytes of data", plaintext.length);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            auditService.logError("DECRYPT", e.getMessage());
            throw new CryptoException("Decryption failed - data may be tampered", e);
        }
    }

    /**
     * Generate a new AES-256 key
     */
    public byte[] generateKey() {
        byte[] key = new byte[AES_KEY_SIZE / 8]; // 32 bytes for AES-256
        secureRandom.nextBytes(key);
        auditService.logKeyGeneration("AES-256");
        return key;
    }

    /**
     * Encrypt a DEK with the KEK (for envelope encryption)
     */
    public String encryptKey(byte[] dek, byte[] kek) {
        try {
            String encrypted = encrypt(Base64.getEncoder().encodeToString(dek), kek);
            auditService.logKeyEncryption("DEK");
            return encrypted;
        } catch (Exception e) {
            auditService.logError("KEY_ENCRYPT", e.getMessage());
            throw new CryptoException("Key encryption failed", e);
        }
    }

    /**
     * Decrypt a DEK with the KEK (for envelope encryption)
     */
    public byte[] decryptKey(String encryptedDek, byte[] kek) {
        try {
            String decrypted = decrypt(encryptedDek, kek);
            auditService.logKeyDecryption("DEK");
            return Base64.getDecoder().decode(decrypted);
        } catch (Exception e) {
            auditService.logError("KEY_DECRYPT", e.getMessage());
            throw new CryptoException("Key decryption failed", e);
        }
    }
}
