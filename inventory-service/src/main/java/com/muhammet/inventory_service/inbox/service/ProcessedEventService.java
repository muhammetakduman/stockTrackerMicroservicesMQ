package com.muhammet.inventory_service.inbox.service;

import com.muhammet.inventory_service.inbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository
            processedEventRepository;

    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public boolean tryRegister(
            UUID eventId,
            String eventType,
            String consumerName
    ) {

        Objects.requireNonNull(
                eventId,
                "Event ID cannot be null"
        );

        if (eventType == null ||
                eventType.isBlank()) {

            throw new IllegalArgumentException(
                    "Event type cannot be blank"
            );
        }

        if (consumerName == null ||
                consumerName.isBlank()) {

            throw new IllegalArgumentException(
                    "Consumer name cannot be blank"
            );
        }

        int insertedRows =
                processedEventRepository.insertIfAbsent(
                        UUID.randomUUID(),
                        eventId,
                        eventType.trim(),
                        consumerName.trim(),
                        Instant.now()
                );

        return insertedRows == 1;
    }
}