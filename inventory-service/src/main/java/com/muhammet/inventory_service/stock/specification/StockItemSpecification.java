package com.muhammet.inventory_service.stock.specification;

import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;

import org.springframework.data.jpa.domain.Specification;

public final class StockItemSpecification {

    private StockItemSpecification() {
    }


    public static Specification<StockItem> hasActive(
            Boolean active
    ) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("active"),
                    active
            );
        };
    }


    public static Specification<StockItem> hasType(
            StockItemType type
    ) {

        return (root, query, criteriaBuilder) -> {

            if (type == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("itemType"),
                    type
            );
        };
    }


    public static Specification<StockItem> hasUnit(
            StockUnit unit
    ) {

        return (root, query, criteriaBuilder) -> {

            if (unit == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("unit"),
                    unit
            );
        };
    }
}