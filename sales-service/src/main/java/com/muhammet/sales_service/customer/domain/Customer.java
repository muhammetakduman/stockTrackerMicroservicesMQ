package com.muhammet.sales_service.customer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(
                        name = "idx_customers_full_name",
                        columnList = "full_name"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    public static Customer create(
            String fullName,
            String notes
    ) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer full name cannot be blank"
            );
        }

        Customer customer = new Customer();

        customer.fullName = fullName.trim();

        customer.notes =
                notes == null || notes.isBlank()
                        ? null
                        : notes.trim();

        customer.active = true;

        return customer;
    }


    public void update(
            String fullName,
            String notes
    ) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer full name cannot be blank"
            );
        }

        this.fullName = fullName.trim();

        this.notes =
                notes == null || notes.isBlank()
                        ? null
                        : notes.trim();
    }


    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }


    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}