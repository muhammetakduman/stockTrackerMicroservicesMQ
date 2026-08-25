package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.AdjustStockRequest;
import com.muhammet.inventory_service.stock.dto.StockAdjustmentResponse;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockMovement;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockAdjustmentService {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;


    @Transactional
    public StockAdjustmentResponse adjust(
            UUID stockItemId,
            AdjustStockRequest request
    ) {

        StockBalance balance =
                stockBalanceRepository
                        .findByStockItemIdForUpdate(stockItemId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Stok bakiyesi bulunamadı: "
                                                + stockItemId
                                )
                        );

        BigDecimal previousQuantity =
                balance.getOnHandQuantity();

        BigDecimal targetQuantity =
                request.targetOnHandQuantity();

        int comparison =
                targetQuantity.compareTo(previousQuantity);

        if (comparison == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Yeni stok miktarı mevcut stok miktarı ile aynı"
            );
        }


        if (comparison > 0) {

            BigDecimal increaseQuantity =
                    targetQuantity.subtract(
                            previousQuantity
                    );

            balance.increaseOnHandQuantity(
                    increaseQuantity
            );

        } else {

            BigDecimal decreaseQuantity =
                    previousQuantity.subtract(
                            targetQuantity
                    );

            try {

                balance.decreaseOnHandQuantity(
                        decreaseQuantity
                );

            } catch (IllegalArgumentException exception) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        exception.getMessage(),
                        exception
                );
            }
        }


        BigDecimal newQuantity =
                balance.getOnHandQuantity();

        UUID adjustmentId =
                UUID.randomUUID();

        Instant now =
                Instant.now();


        String normalizedNote =
                normalizeNote(
                        request.note()
                );


        StockMovement movement =
                StockMovement.adjustment(
                        adjustmentId,
                        balance.getStockItem(),
                        previousQuantity,
                        newQuantity,
                        request.reason().name(),
                        normalizedNote,
                        now
                );


        StockMovement savedMovement =
                stockMovementRepository.save(
                        movement
                );


        return new StockAdjustmentResponse(
                savedMovement.getId(),
                stockItemId,
                previousQuantity,
                newQuantity.subtract(
                        previousQuantity
                ),
                newQuantity,
                balance.getAvailableQuantity(),
                request.reason(),
                normalizedNote,
                now
        );
    }


    private String normalizeNote(
            String note
    ) {

        if (note == null ||
                note.isBlank()) {
            return null;
        }

        return note.trim();
    }
}