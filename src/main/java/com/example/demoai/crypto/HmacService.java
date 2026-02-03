package com.example.demoai.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC-SHA256 Service for Searchable Indexes
 * <p>
 * From the JIO report: HMAC provides deterministic but cryptographically secure
 * indexing for encrypted data. This allows exact-match searches without
 * exposing the plaintext or using deterministic encryption.
 * <p>
 * Key insight: HMAC is deterministic (same input = same output) but secure
 * because it's a one-way function - you cannot reverse the HMAC to get the original.
 */
@Service
public class HmacService {
    private static final Logger log = LoggerFactory.getLogger(HmacService.class);

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final CryptoAuditService auditService;

    public HmacService(CryptoAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Generate HMAC-SHA256 for a value
     * Used for creating searchable indexes on encrypted fields
     *
     * @param value The plaintext value to hash
     * @param key   The HMAC key (should be separate from encryption key!)
     * @return Base64-encoded HMAC
     */
    public String generateHmac(String value, byte[] key) {
        if (value == null) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(hmacBytes);

            auditService.logHmacGeneration();
            log.debug("Generated HMAC for value of {} chars", value.length());

            return result;
        } catch (Exception e) {
            auditService.logError("HMAC", e.getMessage());
            throw new CryptoException("HMAC generation failed", e);
        }
    }

    /**
     * Verify if a value matches an HMAC
     *
     * @param value        The plaintext value to check
     * @param expectedHmac The expected HMAC
     * @param key          The HMAC key
     * @return true if the value produces the same HMAC
     */
    public boolean verifyHmac(String value, String expectedHmac, byte[] key) {
        if (value == null || expectedHmac == null) {
            return false;
        }

        String computedHmac = generateHmac(value, key);
        boolean matches = constantTimeEquals(computedHmac, expectedHmac);

        auditService.logHmacVerification(matches);
        return matches;
    }

    /**
     * Constant-time comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
