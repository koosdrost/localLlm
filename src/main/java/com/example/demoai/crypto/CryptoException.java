package com.example.demoai.crypto;

/**
 * Exception for cryptographic operations
 */
public class CryptoException extends RuntimeException {

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
