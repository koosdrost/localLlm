package com.example.demoai.repository;

import com.example.demoai.entity.SecureData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SecureDataRepository extends ReactiveCrudRepository<SecureData, Long> {

    /**
     * Search by HMAC index (exact match on encrypted field)
     * This is the secure search pattern from the JIO report
     */
    Flux<SecureData> findAllBySensitiveDataHmac(String hmac);

    Flux<SecureData> findByCategory(String category);

    Flux<SecureData> findAllByOrderByCreatedAtDesc();
}
