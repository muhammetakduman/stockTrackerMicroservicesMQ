package com.muhammet.inventory_service.production.repository;

import com.muhammet.inventory_service.production.entity.ProductionBatch;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionBatchRepository
        extends JpaRepository<ProductionBatch, UUID> {

    @EntityGraph(attributePaths = "recipe")
    Optional<ProductionBatch> findByOperationId(
            UUID operationId
    );

    @Override
    @EntityGraph(attributePaths = "recipe")
    Optional<ProductionBatch> findById(
            UUID id
    );

    @EntityGraph(attributePaths = "recipe")
    List<ProductionBatch> findAllByOrderByProducedAtDesc();
}