package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.PackagingStockResponse;
import com.muhammet.inventory_service.stock.dto.PackagingSummaryResponse;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.enums.PackagingKind;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PackagingSummaryService {

    private final StockItemRepository stockItemRepository;
    private final StockBalanceRepository stockBalanceRepository;


    @Transactional(readOnly = true)
    public PackagingSummaryResponse getSummary() {

        return new PackagingSummaryResponse(

                getPackagingStock(
                        PackagingKind.BOTTLE
                ),

                getPackagingStock(
                        PackagingKind.MALE_SET
                ),

                getPackagingStock(
                        PackagingKind.FEMALE_SET
                ),

                getPackagingStock(
                        PackagingKind.UNISEX_SET
                )
        );
    }


    private PackagingStockResponse getPackagingStock(
            PackagingKind packagingKind
    ) {

        StockItem stockItem =
                stockItemRepository
                        .findByPackagingKindAndActiveTrue(
                                packagingKind
                        )
                        .orElse(null);


        if (stockItem == null) {

            return PackagingStockResponse
                    .notConfigured(
                            packagingKind
                    );
        }


        StockBalance balance =
                stockBalanceRepository
                        .findByStockItemId(
                                stockItem.getId()
                        )
                        .orElse(null);


        if (balance == null) {

            return new PackagingStockResponse(

                    packagingKind,

                    true,

                    stockItem.getId(),

                    stockItem.getName(),

                    stockItem.getSku(),

                    java.math.BigDecimal.ZERO,

                    java.math.BigDecimal.ZERO,

                    java.math.BigDecimal.ZERO
            );
        }


        return new PackagingStockResponse(

                packagingKind,

                true,

                stockItem.getId(),

                stockItem.getName(),

                stockItem.getSku(),

                balance.getOnHandQuantity(),

                balance.getReservedQuantity(),

                balance.getAvailableQuantity()
        );
    }
}