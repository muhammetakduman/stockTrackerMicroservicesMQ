package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.InventorySummaryResponse;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class InventorySummaryService {
    @Value("${app.business.time-zone:Europe/Istanbul}")
    private String businessTimeZone;
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


        ZoneId zoneId =
                ZoneId.of(
                        businessTimeZone
                );

        LocalDate today =
                LocalDate.now(
                        zoneId
                );

        Instant startOfToday =
                today
                        .atStartOfDay(zoneId)
                        .toInstant();

        Instant startOfTomorrow =
                today
                        .plusDays(1)
                        .atStartOfDay(zoneId)
                        .toInstant();

        long stockMovementCountToday =
                stockMovementRepository
                        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startOfToday,
                                startOfTomorrow
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