package com.muhammet.sales_service.payment.domain;

import com.muhammet.sales_service.sale.domain.Sale;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(
                        name = "idx_payments_sale_id",
                        columnList = "sale_id"
                ),
                @Index(
                        name = "idx_payments_paid_at",
                        columnList = "paid_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sale_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_payments_sale"
            )
    )
    private Sale sale;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "paid_at",
            nullable = false
    )
    private Instant paidAt;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;

    @Column(
            name = "recorded_by_user_id",
            nullable = false
    )
    private UUID recordedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private PaymentRecordStatus status;

    @Column(
            name = "void_reason",
            length = 500
    )
    private String voidReason;

    @Column(
            name = "voided_by_user_id"
    )
    private UUID voidedByUserId;

    @Column(
            name = "voided_at"
    )
    private Instant voidedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


    private Payment(
            Sale sale,
            BigDecimal amount,
            Instant paidAt,
            String note,
            UUID recordedByUserId
    ) {
        if(sale == null){
            throw new IllegalArgumentException("Sale cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (paidAt == null){
            throw new IllegalArgumentException("PaidAt cannot be null");
        }
        if (recordedByUserId==null){
            throw new IllegalArgumentException("RecordedByUserId cannot be null");
        }
        this.sale = sale;
        this.amount = amount;
        this.paidAt = paidAt;
        this.note = normalizeNote(note);
        this.recordedByUserId = recordedByUserId;
        this.status = PaymentRecordStatus.RECORDED;
        this.voidReason = null;
        this.voidedByUserId = null;
        this.voidedAt = null;
        this.createdAt = Instant.now();
    }

    private static String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmedNote = note.trim();
        return trimmedNote.isEmpty() ? null : trimmedNote;
    }

    public static Payment create(
            Sale sale,
            BigDecimal amount,
            Instant paidAt,
            String note,
            UUID recordedByUserId
    ) {
        return new Payment(
                sale,
                amount,
                paidAt,
                note,
                recordedByUserId
        );
    }

    public void voidPayment(
            String reason,
            UUID voidedByUserId
    ) {
        if (this.status == PaymentRecordStatus.VOIDED) {
            throw new IllegalStateException("Payment is already voided");
        }

        if (reason == null) {
            throw new IllegalArgumentException("Void reason cannot be null");
        }

        String normalizedReason = reason.trim();

        if (normalizedReason.isEmpty()) {
            throw new IllegalArgumentException("Void reason cannot be empty");
        }

        if (voidedByUserId == null) {
            throw new IllegalArgumentException("VoidedByUserId cannot be null");
        }

        this.status = PaymentRecordStatus.VOIDED;
        this.voidReason = normalizedReason;
        this.voidedByUserId = voidedByUserId;
        this.voidedAt = Instant.now();
    }

}