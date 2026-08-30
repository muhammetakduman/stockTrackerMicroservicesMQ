package com.muhammet.inventory_service.production.service;

import com.muhammet.inventory_service.production.dto.request.CreateProductionRecipeRequest;
import com.muhammet.inventory_service.production.dto.response.ProductionRecipeResponse;
import com.muhammet.inventory_service.production.entity.ProductionRecipe;
import com.muhammet.inventory_service.production.repository.ProductionRecipeRepository;

import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.enums.PackagingKind;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;
import com.muhammet.inventory_service.stock.repository.StockItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductionRecipeService {

    private final ProductionRecipeRepository
            productionRecipeRepository;

    private final StockItemRepository
            stockItemRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public ProductionRecipeResponse create(
            CreateProductionRecipeRequest request
    ) {

        validateDistinctStockItems(
                request
        );


        StockItem essenceStockItem =
                getStockItem(
                        request.essenceStockItemId()
                );

        StockItem bottleStockItem =
                getStockItem(
                        request.bottleStockItemId()
                );

        StockItem packagingSetStockItem =
                getStockItem(
                        request.packagingSetStockItemId()
                );

        StockItem outputStockItem =
                getStockItem(
                        request.outputStockItemId()
                );


        validateActive(
                essenceStockItem,
                "Essence"
        );

        validateActive(
                bottleStockItem,
                "Bottle"
        );

        validateActive(
                packagingSetStockItem,
                "Packaging set"
        );

        validateActive(
                outputStockItem,
                "Output"
        );


        validateEssence(
                essenceStockItem
        );

        validateBottle(
                bottleStockItem
        );

        validatePackagingSet(
                packagingSetStockItem
        );

        validateOutput(
                outputStockItem
        );


        validatePieceQuantity(
                request.bottleQuantityPerUnit(),
                "Bottle quantity per unit"
        );

        validatePieceQuantity(
                request.packagingQuantityPerUnit(),
                "Packaging quantity per unit"
        );


        if (productionRecipeRepository
                .existsByOutputStockItem_IdAndActiveTrue(
                        outputStockItem.getId()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active production recipe already exists " +
                            "for this finished product"
            );
        }


        ProductionRecipe recipe =
                ProductionRecipe.create(

                        request.name(),

                        request.description(),

                        essenceStockItem,

                        bottleStockItem,

                        packagingSetStockItem,

                        outputStockItem,

                        request.essenceQuantityPerUnit(),

                        request.bottleQuantityPerUnit(),

                        request.packagingQuantityPerUnit()
                );


        ProductionRecipe saved =
                productionRecipeRepository.save(
                        recipe
                );


        return ProductionRecipeResponse.from(
                saved
        );
    }


    // =========================================================
    // FIND ALL
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProductionRecipeResponse> findAll() {

        return productionRecipeRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(
                        ProductionRecipeResponse::from
                )
                .toList();
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public ProductionRecipeResponse findById(
            UUID id
    ) {

        ProductionRecipe recipe =
                getRecipe(
                        id
                );

        return ProductionRecipeResponse.from(
                recipe
        );
    }


    // =========================================================
    // STATUS
    // =========================================================

    @Transactional
    public ProductionRecipeResponse updateStatus(
            UUID id,
            boolean active
    ) {

        ProductionRecipe recipe =
                getRecipe(
                        id
                );


        if (active) {

            if (!recipe.isActive() &&
                    productionRecipeRepository
                            .existsByOutputStockItem_IdAndActiveTrue(
                                    recipe
                                            .getOutputStockItem()
                                            .getId()
                            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Another active production recipe already " +
                                "exists for this finished product"
                );
            }


            recipe.activate();

        } else {

            recipe.deactivate();
        }


        return ProductionRecipeResponse.from(
                recipe
        );
    }


    // =========================================================
    // GETTERS
    // =========================================================

    private ProductionRecipe getRecipe(
            UUID id
    ) {

        if (id == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe ID cannot be null"
            );
        }


        return productionRecipeRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Production recipe not found: " + id
                        )
                );
    }


    private StockItem getStockItem(
            UUID id
    ) {

        return stockItemRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Stock item not found: " + id
                        )
                );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateDistinctStockItems(
            CreateProductionRecipeRequest request
    ) {

        Set<UUID> stockItemIds =
                new HashSet<>();

        stockItemIds.add(
                request.essenceStockItemId()
        );

        stockItemIds.add(
                request.bottleStockItemId()
        );

        stockItemIds.add(
                request.packagingSetStockItemId()
        );

        stockItemIds.add(
                request.outputStockItemId()
        );


        if (stockItemIds.size() != 4) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Essence, bottle, packaging set and output " +
                            "must be different stock items"
            );
        }
    }


    private void validateActive(
            StockItem stockItem,
            String label
    ) {

        if (!stockItem.isActive()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " stock item must be active"
            );
        }
    }


    private void validateEssence(
            StockItem stockItem
    ) {

        if (stockItem.getItemType() !=
                StockItemType.ESSENCE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Essence stock item must have ESSENCE type"
            );
        }


        if (stockItem.getUnit() !=
                StockUnit.GRAM) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Essence stock item must use GRAM unit"
            );
        }
    }


    private void validateBottle(
            StockItem stockItem
    ) {

        if (stockItem.getItemType() !=
                StockItemType.PACKAGING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bottle stock item must have PACKAGING type"
            );
        }


        if (stockItem.getUnit() !=
                StockUnit.PIECE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bottle stock item must use PIECE unit"
            );
        }


        if (stockItem.getPackagingKind() !=
                PackagingKind.BOTTLE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bottle stock item must have BOTTLE packaging kind"
            );
        }
    }


    private void validatePackagingSet(
            StockItem stockItem
    ) {

        if (stockItem.getItemType() !=
                StockItemType.PACKAGING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Packaging set must have PACKAGING type"
            );
        }


        if (stockItem.getUnit() !=
                StockUnit.PIECE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Packaging set must use PIECE unit"
            );
        }


        PackagingKind packagingKind =
                stockItem.getPackagingKind();


        if (packagingKind != PackagingKind.MALE_SET &&
                packagingKind != PackagingKind.FEMALE_SET &&
                packagingKind != PackagingKind.UNISEX_SET) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Packaging set must be MALE_SET, " +
                            "FEMALE_SET or UNISEX_SET"
            );
        }
    }


    private void validateOutput(
            StockItem stockItem
    ) {

        if (stockItem.getItemType() !=
                StockItemType.FINISHED_PRODUCT) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Output stock item must have FINISHED_PRODUCT type"
            );
        }


        if (stockItem.getUnit() !=
                StockUnit.PIECE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Output stock item must use PIECE unit"
            );
        }
    }


    private void validatePieceQuantity(
            BigDecimal quantity,
            String fieldName
    ) {

        if (quantity == null ||
                quantity.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be greater than zero"
            );
        }


        if (quantity.stripTrailingZeros().scale() > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a whole number"
            );
        }
    }
}