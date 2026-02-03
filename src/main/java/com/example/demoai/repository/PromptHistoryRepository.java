package com.example.demoai.repository;

import com.example.demoai.entity.PromptHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PromptHistoryRepository extends ReactiveCrudRepository<PromptHistory, Long> {

    Flux<PromptHistory> findAllByOrderByTimestampDesc();
}
