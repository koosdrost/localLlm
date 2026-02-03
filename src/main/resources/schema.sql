-- Prompt history table
CREATE TABLE IF NOT EXISTS prompt_history
(
    id
    SERIAL
    PRIMARY
    KEY,
    prompt
    TEXT
    NOT
    NULL,
    response
    TEXT,
    timestamp
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP
);

-- Encryption keys table (stores encrypted DEKs)
-- From JIO report: DEKs stored in database, encrypted with KEK
CREATE TABLE IF NOT EXISTS encryption_keys
(
    id
    SERIAL
    PRIMARY
    KEY,
    table_name
    VARCHAR
(
    255
) NOT NULL,
    key_type VARCHAR
(
    50
) NOT NULL, -- 'DEK' or 'HMAC'
    encrypted_key TEXT NOT NULL, -- Key encrypted with KEK (Base64)
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rotated_at TIMESTAMP
    );

-- Index for fast key lookup
CREATE INDEX IF NOT EXISTS idx_encryption_keys_lookup
    ON encryption_keys(table_name, key_type, active);

-- Secure data table (demonstrates encrypted storage)
-- From JIO report Option 3: AES-GCM + HMAC pattern
CREATE TABLE IF NOT EXISTS secure_data
(
    id
    SERIAL
    PRIMARY
    KEY,
    sensitive_data
    TEXT, -- Encrypted with AES-GCM (non-deterministic)
    sensitive_data_hmac
    VARCHAR
(
    64
), -- HMAC index for searching (deterministic)
    category VARCHAR
(
    255
),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
    );

-- Index on HMAC for efficient searching
CREATE INDEX IF NOT EXISTS idx_secure_data_hmac
    ON secure_data(sensitive_data_hmac);

CREATE INDEX IF NOT EXISTS idx_secure_data_category
    ON secure_data(category);
