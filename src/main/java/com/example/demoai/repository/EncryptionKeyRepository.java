package com.example.demoai.repository;

import com.example.demoai.entity.EncryptionKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EncryptionKeyRepository extends ReactiveCrudRepository<EncryptionKey, Long> {

    Mono<EncryptionKey> findByTableNameAndKeyTypeAndActiveTrue(String tableName, String keyType);
}
