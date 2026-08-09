package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.CreateStockItemRequest;
import com.muhammet.inventory_service.stock.dto.StockItemResponse;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockItemService {

    private final StockItemRepository stockItemRepository;

    @Transactional
    public StockItemResponse create(CreateStockItemRequest request) {

        String normalizedSku = request.sku()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (stockItemRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bu SKU ile daha önce stok kalemi oluşturulmuş: " + normalizedSku
            );
        }

        StockItem stockItem = new StockItem();
        stockItem.setName(request.name().trim());
        stockItem.setSku(normalizedSku);
        stockItem.setDescription(normalizeDescription(request.description()));
        stockItem.setItemType(request.itemType());
        stockItem.setUnit(request.unit());
        stockItem.setActive(true);

        StockBalance balance = new StockBalance(stockItem);

        stockItem.assignBalance(balance);

        StockItem savedStockItem = stockItemRepository.save(stockItem);

        return toResponse(savedStockItem);
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> findAll() {
        return stockItemRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockItemResponse findById(UUID id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stok kalemi bulunamadı: " + id
                ));

        return toResponse(stockItem);
    }

    private StockItemResponse toResponse(StockItem stockItem) {
        StockBalance balance = stockItem.getBalance();

        return new StockItemResponse(
                stockItem.getId(),
                stockItem.getName(),
                stockItem.getSku(),
                stockItem.getDescription(),
                stockItem.getItemType(),
                stockItem.getUnit(),
                balance.getOnHandQuantity(),
                balance.getReservedQuantity(),
                balance.getAvailableQuantity(),
                stockItem.isActive(),
                stockItem.getCreatedAt(),
                stockItem.getUpdatedAt()
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}