package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockItemRepository
        extends JpaRepository<StockItem, UUID> {

    boolean existsBySkuIgnoreCase(String sku);

    List<StockItem> findAllByOrderByNameAsc();
}