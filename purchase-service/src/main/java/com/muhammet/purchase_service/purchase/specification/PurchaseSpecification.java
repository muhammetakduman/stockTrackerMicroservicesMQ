package com.muhammet.purchase_service.purchase.specification;

import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class PurchaseSpecification {

    private PurchaseSpecification() {
    }

    public static Specification<Purchase> hasStatus(
            PurchaseStatus status
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


    public static Specification<Purchase> hasStockItemId(
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


    public static Specification<Purchase> hasSupplierName(
            String supplierName
    ) {

        return (root, query, cb) -> {

            if (supplierName == null ||
                    supplierName.isBlank()) {

                return cb.conjunction();
            }

            String normalized =
                    "%" +
                            supplierName
                                    .trim()
                                    .toLowerCase(Locale.ROOT)
                            + "%";

            return cb.like(
                    cb.lower(
                            root.get("supplierName")
                    ),
                    normalized
            );
        };
    }


    public static Specification<Purchase> purchasedFrom(
            Instant from
    ) {

        return (root, query, cb) -> {

            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("purchasedAt"),
                    from
            );
        };
    }


    public static Specification<Purchase> purchasedTo(
            Instant to
    ) {

        return (root, query, cb) -> {

            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("purchasedAt"),
                    to
            );
        };
    }
}