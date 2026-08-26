package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.StockBalanceResponse;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;


    @Transactional(readOnly = true)
    public List<StockBalanceResponse> findAll() {

        return stockBalanceRepository
                .findAllWithStockItem()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private StockBalanceResponse toResponse(
            StockBalance balance
    ) {

        StockItem stockItem =
                balance.getStockItem();

        return new StockBalanceResponse(
                balance.getId(),

                stockItem.getId(),
                stockItem.getName(),
                stockItem.getSku(),
                stockItem.getItemType(),
                stockItem.getUnit(),

                balance.getOnHandQuantity(),
                balance.getReservedQuantity(),
                balance.getAvailableQuantity(),

                stockItem.isActive(),

                balance.getUpdatedAt()
        );
    }
    @Transactional(readOnly = true)
    public List<StockBalanceResponse> findLowStock(
            BigDecimal threshold
    ) {

        if (threshold == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Threshold değeri zorunludur"
            );
        }

        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Threshold negatif olamaz"
            );
        }

        return stockBalanceRepository
                .findLowStockBalances(threshold)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}