package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.CreateStockItemRequest;
import com.muhammet.inventory_service.stock.dto.StockItemResponse;
import com.muhammet.inventory_service.stock.dto.UpdateStockItemRequest;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.enums.PackagingKind;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;
import com.muhammet.inventory_service.stock.specification.StockItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        // PACKAGING tipi için PackagingKind zorunlu; diğerleri için null olmalı
        validatePackagingKind(request.itemType(), request.packagingKind(), request.unit());

        StockItem stockItem = new StockItem();
        stockItem.setName(request.name().trim());
        stockItem.setSku(normalizedSku);
        stockItem.setDescription(normalizeDescription(request.description()));
        stockItem.setItemType(request.itemType());
        stockItem.setUnit(request.unit());
        stockItem.setPackagingKind(request.packagingKind());
        stockItem.setActive(true);

        StockBalance balance = new StockBalance(stockItem);
        stockItem.assignBalance(balance);

        StockItem savedStockItem = stockItemRepository.save(stockItem);

        return toResponse(savedStockItem);
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> findAll(
            Boolean active,
            StockItemType type,
            StockUnit unit
    ) {

        Specification<StockItem> specification =
                Specification
                        .where(
                                StockItemSpecification
                                        .hasActive(active)
                        )
                        .and(
                                StockItemSpecification
                                        .hasType(type)
                        )
                        .and(
                                StockItemSpecification
                                        .hasUnit(unit)
                        );


        Sort sort =
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                );


        return stockItemRepository
                .findAll(
                        specification,
                        sort
                )
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
                stockItem.getPackagingKind(),
                balance.getOnHandQuantity(),
                balance.getReservedQuantity(),
                balance.getAvailableQuantity(),
                stockItem.isActive(),
                stockItem.getCreatedAt(),
                stockItem.getUpdatedAt()
        );
    }

    /**
     * PACKAGING tipi için packagingKind zorunlu ve unit PIECE olmalı.
     * Diğer tipler için packagingKind null olmalı.
     */
    private void validatePackagingKind(
            StockItemType itemType,
            PackagingKind packagingKind,
            StockUnit unit
    ) {
        if (itemType == StockItemType.PACKAGING) {
            if (packagingKind == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "PACKAGING tipindeki stok kalemleri için packagingKind belirtilmelidir " +
                                "(BOTTLE, MALE_SET, FEMALE_SET, UNISEX_SET)"
                );
            }
            if (unit != StockUnit.PIECE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "PACKAGING tipindeki stok kalemleri PIECE birimini kullanmalıdır"
                );
            }
        } else {
            if (packagingKind != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "packagingKind yalnızca PACKAGING tipindeki stok kalemleri için kullanılabilir"
                );
            }
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
    @Transactional
    public StockItemResponse updateStatus(
            UUID id,
            boolean active
    ) {

        StockItem stockItem =
                stockItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Stok kalemi bulunamadı: " + id
                                )
                        );

        stockItem.setActive(active);

        return toResponse(stockItem);
    }
    @Transactional
    public StockItemResponse update(
            UUID id,
            UpdateStockItemRequest request
    ) {

        StockItem stockItem =
                stockItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Stok kalemi bulunamadı: " + id
                                )
                        );


        if (request.name() == null &&
                request.description() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Güncellenecek en az bir alan gönderilmelidir"
            );
        }


        /*
         * NAME
         */
        if (request.name() != null) {

            String normalizedName =
                    request.name().trim();

            if (normalizedName.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stok kalemi adı boş olamaz"
                );
            }

            stockItem.setName(
                    normalizedName
            );
        }


        /*
         * DESCRIPTION
         */
        if (request.description() != null) {

            stockItem.setDescription(
                    normalizeDescription(
                            request.description()
                    )
            );
        }


        /*
         * save() gerekli değil.
         *
         * stockItem managed entity olduğu için Hibernate
         * transaction commit sırasında dirty checking
         * ile UPDATE çalıştıracaktır.
         */
        return toResponse(
                stockItem
        );
    }
}