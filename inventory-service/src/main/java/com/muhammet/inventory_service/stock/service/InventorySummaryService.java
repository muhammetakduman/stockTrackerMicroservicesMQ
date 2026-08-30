package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.InventorySummaryResponse;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class InventorySummaryService {

    private final StockItemRepository stockItemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;


    @Transactional(readOnly = true)
    public InventorySummaryResponse getSummary() {

        long totalStockItems =
                stockItemRepository.count();

        long activeStockItems =
                stockItemRepository.countByActiveTrue();

        long inactiveStockItems =
                stockItemRepository.countByActiveFalse();

        long outOfStockItems =
                stockBalanceRepository.countOutOfStock();


        Instant startOfToday =
                LocalDate.now(ZoneOffset.UTC)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC);


        long stockMovementCountToday =
                stockMovementRepository
                        .countByCreatedAtGreaterThanEqual(
                                startOfToday
                        );


        return new InventorySummaryResponse(
                totalStockItems,
                activeStockItems,
                inactiveStockItems,
                outOfStockItems,
                stockMovementCountToday
        );
    }
}