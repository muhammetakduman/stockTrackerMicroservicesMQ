package com.muhammet.inventory_service.stock.enums;

/**
 * PACKAGING tipindeki StockItem'ların alt sınıflandırması.
 *
 * Yalnızca itemType = PACKAGING olan StockItem'larda kullanılır.
 * Diğer tipler (ESSENCE, FINISHED_PRODUCT) için null olmalıdır.
 */
public enum PackagingKind {
    BOTTLE,
    MALE_SET,
    FEMALE_SET,
    UNISEX_SET
}

