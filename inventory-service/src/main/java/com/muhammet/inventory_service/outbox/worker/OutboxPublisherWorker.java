package com.muhammet.inventory_service.outbox.worker;


import com.muhammet.inventory_service.outbox.config.OutboxPublisherProperties;
import com.muhammet.inventory_service.outbox.entity.OutboxEvent;
import com.muhammet.inventory_service.outbox.enums.OutboxStatus;
import com.muhammet.inventory_service.outbox.repository.OutboxEventRepository;
import com.muhammet.inventory_service.outbox.service.OutboxEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherWorker {
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventProcessor outboxEventProcessor;
    private final OutboxPublisherProperties properties;

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:2000}")
    public void publishPendingOutboxEvents(){
        List<UUID> eventIds = outboxEventRepository.findAllByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                Instant.now(),
                PageRequest.of(
                        0, properties.getBatchSize()
                )
        )
                .stream()
                .map(OutboxEvent::getId)
                .toList();

        if (eventIds.isEmpty()){
            return;
        }
        log.info("Inventory outbox batch found size={}",
                eventIds.size());
        for(UUID eventId : eventIds){
            try{
                outboxEventProcessor.process(eventId);
            } catch (RuntimeException exception){
                log.error("Unexpected inventory outbox procesing failure.But decrease stock update " + "outboxId={}", eventId, exception);
            }
        }
    }
}
