package com.muhammet.inventory_service.stock.entity;

import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "stock_items",
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(
                        name = "uk_stock_items_sku",
                        columnNames = "sku"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class StockItem {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 150)
    private String name;

    @Column(nullable = false,length = 50 , unique = true)
    private String sku;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private StockItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 30)
    private StockUnit unit;

    @Column(nullable = false)
    private boolean active = true;

    @Column (name ="created_at" , nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(
            mappedBy = "stockItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            optional = false
    )
    private StockBalance balance;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    public void assignBalance(StockBalance balance) {
        if (balance == null) {
            throw new IllegalArgumentException(
                    "Stock balance cannot be null"
            );
        }

        this.balance = balance;
        balance.assignStockItem(this);
    }

}
