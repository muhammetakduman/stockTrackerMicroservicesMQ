package com.muhammet.inventory_service.production.repository;

import com.muhammet.inventory_service.production.entity.ProductionRecipe;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionRecipeRepository
        extends JpaRepository<ProductionRecipe, UUID> {


    @Override
    @EntityGraph(attributePaths = {
            "essenceStockItem",
            "bottleStockItem",
            "packagingSetStockItem",
            "outputStockItem"
    })
    Optional<ProductionRecipe> findById(
            UUID id
    );


    @EntityGraph(attributePaths = {
            "essenceStockItem",
            "bottleStockItem",
            "packagingSetStockItem",
            "outputStockItem"
    })
    List<ProductionRecipe> findAllByOrderByNameAsc();


    boolean existsByOutputStockItem_IdAndActiveTrue(
            UUID outputStockItemId
    );
}