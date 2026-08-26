package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.StockBalanceResponse;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}