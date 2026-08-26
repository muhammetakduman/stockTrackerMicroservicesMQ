package com.muhammet.inventory_service.stock.specification;

import com.muhammet.inventory_service.stock.entity.StockMovement;
import com.muhammet.inventory_service.stock.enums.StockMovementType;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class StockMovementSpecification {

    private StockMovementSpecification() {
    }


    public static Specification<StockMovement> hasType(
            StockMovementType type
    ) {

        return (root, query, criteriaBuilder) -> {

            if (type == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("movementType"),
                    type
            );
        };
    }


    public static Specification<StockMovement> hasStockItemId(
            UUID stockItemId
    ) {

        return (root, query, criteriaBuilder) -> {

            if (stockItemId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("stockItem").get("id"),
                    stockItemId
            );
        };
    }


    public static Specification<StockMovement> occurredFrom(
            Instant from
    ) {

        return (root, query, criteriaBuilder) -> {

            if (from == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("sourceOccurredAt"),
                    from
            );
        };
    }


    public static Specification<StockMovement> occurredTo(
            Instant to
    ) {

        return (root, query, criteriaBuilder) -> {

            if (to == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("sourceOccurredAt"),
                    to
            );
        };
    }
}