package com.muhammet.sales_service.sale.specification;

import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.domain.SaleStatus;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class SaleSpecification {

    private SaleSpecification() {
    }


    public static Specification<Sale> hasStatus(
            SaleStatus status
    ) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }


    public static Specification<Sale> hasSellerId(
            Long sellerId
    ) {

        return (root, query, cb) -> {

            if (sellerId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("sellerId"),
                    sellerId
            );
        };
    }


    public static Specification<Sale> hasStockItemId(
            UUID stockItemId
    ) {

        return (root, query, cb) -> {

            if (stockItemId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("stockItemId"),
                    stockItemId
            );
        };
    }


    public static Specification<Sale> soldFrom(
            Instant from
    ) {

        return (root, query, cb) -> {

            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("soldAt"),
                    from
            );
        };
    }


    public static Specification<Sale> soldTo(
            Instant to
    ) {

        return (root, query, cb) -> {

            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("soldAt"),
                    to
            );
        };
    }
}