package com.muhammet.inventory_service.production.service;

import com.muhammet.inventory_service.production.dto.request.CreateProductionBatchRequest;
import com.muhammet.inventory_service.production.dto.response.ProductionBatchResponse;
import com.muhammet.inventory_service.production.entity.ProductionBatch;
import com.muhammet.inventory_service.production.entity.ProductionRecipe;
import com.muhammet.inventory_service.production.repository.ProductionBatchRepository;
import com.muhammet.inventory_service.production.repository.ProductionRecipeRepository;

import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.entity.StockMovement;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;
import com.muhammet.inventory_service.stock.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionBatchService {

    private final ProductionBatchRepository
            productionBatchRepository;

    private final ProductionRecipeRepository
            productionRecipeRepository;

    private final StockBalanceRepository
            stockBalanceRepository;

    private final StockMovementRepository
            stockMovementRepository;

    private final AuthenticatedUser
            authenticatedUser;


    // =========================================================
    // CREATE PRODUCTION
    // =========================================================

    @Transactional
    public ProductionBatchResponse create(
            CreateProductionBatchRequest request
    ) {

        /*
         * Fast idempotency check.
         *
         * Aynı operationId daha önce başarılı bir şekilde
         * işlendiyse stoklara tekrar dokunmayız.
         */
        Optional<ProductionBatch> existingBatch =
                productionBatchRepository.findByOperationId(
                        request.operationId()
                );

        if (existingBatch.isPresent()) {

            validateSameOperation(
                    existingBatch.get(),
                    request
            );

            return ProductionBatchResponse.from(
                    existingBatch.get()
            );
        }


        validateOutputQuantity(
                request.outputQuantity()
        );


        ProductionRecipe recipe =
                productionRecipeRepository
                        .findById(
                                request.recipeId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Production recipe not found: "
                                                + request.recipeId()
                                )
                        );


        if (!recipe.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Production recipe is not active"
            );
        }


        validateRecipeStockItemsActive(
                recipe
        );


        /*
         * Recipe:
         *
         * 10 GR essence
         * 1 bottle
         * 1 packaging
         *
         * outputQuantity = 5
         *
         * => 50 GR / 5 bottle / 5 packaging
         */
        BigDecimal essenceRequired =
                recipe
                        .getEssenceQuantityPerUnit()
                        .multiply(
                                request.outputQuantity()
                        );

        BigDecimal bottleRequired =
                recipe
                        .getBottleQuantityPerUnit()
                        .multiply(
                                request.outputQuantity()
                        );

        BigDecimal packagingRequired =
                recipe
                        .getPackagingQuantityPerUnit()
                        .multiply(
                                request.outputQuantity()
                        );


        UUID essenceId =
                recipe
                        .getEssenceStockItem()
                        .getId();

        UUID bottleId =
                recipe
                        .getBottleStockItem()
                        .getId();

        UUID packagingId =
                recipe
                        .getPackagingSetStockItem()
                        .getId();

        UUID outputId =
                recipe
                        .getOutputStockItem()
                        .getId();


        /*
         * Recipe bozuk/veritabanına manuel girilmiş bile olsa
         * aynı StockItem iki farklı görevde kullanılamaz.
         */
        Set<UUID> distinctIds =
                Set.of(
                        essenceId,
                        bottleId,
                        packagingId,
                        outputId
                );

        if (distinctIds.size() != 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Production recipe contains duplicate stock items"
            );
        }


        /*
         * Deadlock riskini azaltmak için her production
         * transaction aynı UUID sırasını kullanır.
         */
        List<UUID> stockItemIds =
                new ArrayList<>(
                        distinctIds
                );

        stockItemIds.sort(
                UUID::compareTo
        );


        /*
         * EN ÖNEMLİ NOKTA:
         *
         * Essence
         * Bottle
         * Packaging
         * Finished product
         *
         * dört balance tek query ile WRITE lock altına alınır.
         */
        List<StockBalance> lockedBalances =
                stockBalanceRepository
                        .findAllByStockItemIdsForUpdate(
                                stockItemIds
                        );


        if (lockedBalances.size() != 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "One or more stock balances could not be found"
            );
        }


        /*
         * Concurrent aynı request senaryosu için ikinci
         * idempotency kontrolü.
         *
         * İlk transaction lock'ları bırakana kadar ikinci
         * transaction beklemiş olabilir.
         */
        Optional<ProductionBatch> concurrentExistingBatch =
                productionBatchRepository
                        .findByOperationId(
                                request.operationId()
                        );

        if (concurrentExistingBatch.isPresent()) {

            validateSameOperation(
                    concurrentExistingBatch.get(),
                    request
            );

            return ProductionBatchResponse.from(
                    concurrentExistingBatch.get()
            );
        }


        Map<UUID, StockBalance> balanceMap =
                lockedBalances
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        balance ->
                                                balance
                                                        .getStockItem()
                                                        .getId(),

                                        Function.identity()
                                )
                        );


        StockBalance essenceBalance =
                getLockedBalance(
                        balanceMap,
                        essenceId,
                        "Essence"
                );

        StockBalance bottleBalance =
                getLockedBalance(
                        balanceMap,
                        bottleId,
                        "Bottle"
                );

        StockBalance packagingBalance =
                getLockedBalance(
                        balanceMap,
                        packagingId,
                        "Packaging set"
                );

        StockBalance outputBalance =
                getLockedBalance(
                        balanceMap,
                        outputId,
                        "Output"
                );


        // =====================================================
        // AVAILABLE STOCK VALIDATION
        // =====================================================

        validateAvailableStock(
                essenceBalance,
                essenceRequired,
                "Essence"
        );

        validateAvailableStock(
                bottleBalance,
                bottleRequired,
                "Bottle"
        );

        validateAvailableStock(
                packagingBalance,
                packagingRequired,
                "Packaging set"
        );


        /*
         * Movement snapshotları için mutation öncesindeki
         * değerleri tutuyoruz.
         */
        BigDecimal essenceBefore =
                essenceBalance.getOnHandQuantity();

        BigDecimal bottleBefore =
                bottleBalance.getOnHandQuantity();

        BigDecimal packagingBefore =
                packagingBalance.getOnHandQuantity();

        BigDecimal outputBefore =
                outputBalance.getOnHandQuantity();


        // =====================================================
        // STOCK MUTATION
        // =====================================================

        essenceBalance.decreaseOnHandQuantity(
                essenceRequired
        );

        bottleBalance.decreaseOnHandQuantity(
                bottleRequired
        );

        packagingBalance.decreaseOnHandQuantity(
                packagingRequired
        );

        outputBalance.increaseOnHandQuantity(
                request.outputQuantity()
        );


        // =====================================================
        // BATCH
        // =====================================================

        UUID producedByUserId =
                authenticatedUser
                        .getCurrentUserId();


        ProductionBatch batch =
                ProductionBatch.create(

                        request.operationId(),

                        recipe,

                        request.outputQuantity(),

                        essenceRequired,

                        bottleRequired,

                        packagingRequired,

                        producedByUserId,

                        request.note()
                );


        /*
         * StockMovement sourceEventId oluşturmak için batch ID
         * gerekiyor.
         *
         * GenerationType.UUID id'nin kesin oluşmasını sağlamak
         * için flush ediyoruz.
         *
         * Transaction yine henüz COMMIT olmuyor.
         */
        ProductionBatch savedBatch =
                productionBatchRepository
                        .saveAndFlush(
                                batch
                        );


        UUID batchId =
                savedBatch.getId();


        if (batchId == null) {
            throw new IllegalStateException(
                    "Production batch ID was not generated"
            );
        }


        Instant occurredAt =
                savedBatch.getProducedAt();


        // =====================================================
        // STOCK MOVEMENTS
        // =====================================================

        StockMovement essenceMovement =
                StockMovement.productionConsumption(

                        batchId,

                        essenceBalance.getStockItem(),

                        essenceRequired,

                        essenceBefore,

                        essenceBalance.getOnHandQuantity(),

                        occurredAt
                );


        StockMovement bottleMovement =
                StockMovement.productionConsumption(

                        batchId,

                        bottleBalance.getStockItem(),

                        bottleRequired,

                        bottleBefore,

                        bottleBalance.getOnHandQuantity(),

                        occurredAt
                );


        StockMovement packagingMovement =
                StockMovement.productionConsumption(

                        batchId,

                        packagingBalance.getStockItem(),

                        packagingRequired,

                        packagingBefore,

                        packagingBalance.getOnHandQuantity(),

                        occurredAt
                );


        StockMovement outputMovement =
                StockMovement.productionOutput(

                        batchId,

                        outputBalance.getStockItem(),

                        request.outputQuantity(),

                        outputBefore,

                        outputBalance.getOnHandQuantity(),

                        occurredAt
                );


        stockMovementRepository.saveAll(
                List.of(
                        essenceMovement,
                        bottleMovement,
                        packagingMovement,
                        outputMovement
                )
        );


        /*
         * StockBalance entity'leri transaction içerisinde
         * managed durumda.
         *
         * save() çağırmak zorunda değiliz.
         * Hibernate dirty checking ile UPDATE edecektir.
         */


        return ProductionBatchResponse.from(
                savedBatch
        );
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public ProductionBatchResponse findById(
            UUID id
    ) {

        ProductionBatch batch =
                productionBatchRepository
                        .findById(
                                id
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Production batch not found: "
                                                + id
                                )
                        );


        return ProductionBatchResponse.from(
                batch
        );
    }


    // =========================================================
    // LIST
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProductionBatchResponse> findAll() {

        return productionBatchRepository
                .findAllByOrderByProducedAtDesc()
                .stream()
                .map(
                        ProductionBatchResponse::from
                )
                .toList();
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateOutputQuantity(
            BigDecimal outputQuantity
    ) {

        if (outputQuantity == null ||
                outputQuantity.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Output quantity must be greater than zero"
            );
        }


        /*
         * Finished product unit = PIECE.
         *
         * 5    -> OK
         * 5.0  -> OK
         * 5.5  -> ERROR
         */
        if (outputQuantity
                .stripTrailingZeros()
                .scale() > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Output quantity must be a whole number"
            );
        }
    }


    private void validateAvailableStock(
            StockBalance balance,
            BigDecimal requiredQuantity,
            String label
    ) {

        BigDecimal available =
                balance.getAvailableQuantity();


        if (available.compareTo(
                requiredQuantity
        ) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,

                    "Insufficient " +
                            label +
                            " stock. available=" +
                            available +
                            ", required=" +
                            requiredQuantity
            );
        }
    }


    private StockBalance getLockedBalance(
            Map<UUID, StockBalance> balances,
            UUID stockItemId,
            String label
    ) {

        StockBalance balance =
                balances.get(
                        stockItemId
                );


        if (balance == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    label +
                            " stock balance could not be found"
            );
        }


        return balance;
    }


    private void validateRecipeStockItemsActive(
            ProductionRecipe recipe
    ) {

        validateActive(
                recipe.getEssenceStockItem(),
                "Essence"
        );

        validateActive(
                recipe.getBottleStockItem(),
                "Bottle"
        );

        validateActive(
                recipe.getPackagingSetStockItem(),
                "Packaging set"
        );

        validateActive(
                recipe.getOutputStockItem(),
                "Output"
        );
    }


    private void validateActive(
            StockItem stockItem,
            String label
    ) {

        if (!stockItem.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    label + " stock item is inactive"
            );
        }
    }


    /*
     * Aynı operationId farklı bir request için
     * tekrar kullanılamaz.
     */
    private void validateSameOperation(
            ProductionBatch existing,
            CreateProductionBatchRequest request
    ) {

        boolean sameRecipe =
                existing
                        .getRecipe()
                        .getId()
                        .equals(
                                request.recipeId()
                        );

        boolean sameQuantity =
                existing
                        .getOutputQuantity()
                        .compareTo(
                                request.outputQuantity()
                        ) == 0;


        if (!sameRecipe ||
                !sameQuantity) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Operation ID has already been used " +
                            "for a different production request"
            );
        }
    }
}