package com.muhammet.sales_service.payment.service;

import com.muhammet.sales_service.payment.domain.Payment;
import com.muhammet.sales_service.payment.domain.SalePaymentStatus;
import com.muhammet.sales_service.payment.dto.request.CreatePaymentRequest;
import com.muhammet.sales_service.payment.dto.response.PaymentOperationResponse;
import com.muhammet.sales_service.payment.dto.response.PaymentResponse;
import com.muhammet.sales_service.payment.dto.response.SalePaymentSummaryResponse;
import com.muhammet.sales_service.payment.repository.PaymentRepository;
import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.domain.SaleStatus;
import com.muhammet.sales_service.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;


    // ----------------------------------------------------
    // PAYMENT CREATE
    // ----------------------------------------------------

    @Transactional
    public PaymentOperationResponse recordPayment(
            Long saleId,
            CreatePaymentRequest request,
            UUID actorUserId,
            boolean admin
    ) {

        Sale sale = saleRepository.findByIdForUpdate(saleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sale not found"
                        )
                );

        assertCanAccessSale(
                sale,
                actorUserId,
                admin
        );

        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payments can only be recorded for completed sales"
            );
        }

        BigDecimal totalPaid =
                paymentRepository.sumRecordedAmountBySaleId(
                        saleId
                );

        BigDecimal outstanding =
                sale.getTotalPrice()
                        .subtract(totalPaid);

        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sale is already fully paid"
            );
        }

        if (request.amount().compareTo(outstanding) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment amount cannot exceed outstanding amount"
            );
        }

        Payment payment = Payment.create(
                sale,
                request.amount(),
                Instant.now(),
                request.note(),
                actorUserId
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        BigDecimal newTotalPaid =
                totalPaid.add(request.amount());

        SalePaymentSummaryResponse summary =
                buildSummary(
                        sale,
                        newTotalPaid
                );

        return new PaymentOperationResponse(
                PaymentResponse.from(savedPayment),
                summary
        );
    }


    // ----------------------------------------------------
    // PAYMENT HISTORY
    // ----------------------------------------------------

    @Transactional(readOnly = true)
    public List<PaymentResponse> findPaymentsBySale(
            Long saleId,
            UUID actorUserId,
            boolean admin
    ) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sale not found"
                        )
                );

        assertCanAccessSale(
                sale,
                actorUserId,
                admin
        );

        return paymentRepository
                .findAllBySale_IdOrderByPaidAtAsc(saleId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }


    // ----------------------------------------------------
    // SALE PAYMENT SUMMARY
    // ----------------------------------------------------

    @Transactional(readOnly = true)
    public SalePaymentSummaryResponse getSalePaymentSummary(
            Long saleId,
            UUID actorUserId,
            boolean admin
    ) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sale not found"
                        )
                );

        assertCanAccessSale(
                sale,
                actorUserId,
                admin
        );

        BigDecimal totalPaid =
                paymentRepository
                        .sumRecordedAmountBySaleId(
                                saleId
                        );

        return buildSummary(
                sale,
                totalPaid
        );
    }


    // ----------------------------------------------------
    // ADMIN PAYMENT VOID
    // ----------------------------------------------------

    @Transactional
    public PaymentOperationResponse voidPayment(
            UUID paymentId,
            String reason,
            UUID adminUserId
    ) {

        Payment existingPayment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment not found"
                                )
                        );

        Long saleId =
                existingPayment
                        .getSale()
                        .getId();

        // Lock ordering:
        // First Sale -> then Payment
        Sale sale =
                saleRepository.findByIdForUpdate(saleId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Sale not found"
                                )
                        );

        Payment payment =
                paymentRepository.findByIdForUpdate(paymentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment not found"
                                )
                        );

        try {

            payment.voidPayment(
                    reason,
                    adminUserId
            );

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }

        BigDecimal totalPaid =
                paymentRepository
                        .sumRecordedAmountBySaleId(
                                saleId
                        );

        SalePaymentSummaryResponse summary =
                buildSummary(
                        sale,
                        totalPaid
                );

        return new PaymentOperationResponse(
                PaymentResponse.from(payment),
                summary
        );
    }


    // ----------------------------------------------------
    // ADMIN LIST
    // ----------------------------------------------------

    @Transactional(readOnly = true)
    public Page<PaymentResponse> findAllPayments(
            Pageable pageable
    ) {

        return paymentRepository
                .findAll(pageable)
                .map(PaymentResponse::from);
    }


    // ----------------------------------------------------
    // ADMIN DETAIL
    // ----------------------------------------------------

    @Transactional(readOnly = true)
    public PaymentResponse findPaymentById(
            UUID paymentId
    ) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment not found"
                                )
                        );

        return PaymentResponse.from(payment);
    }


    // ----------------------------------------------------
    // SUMMARY BUILDER
    // ----------------------------------------------------

    private SalePaymentSummaryResponse buildSummary(
            Sale sale,
            BigDecimal totalPaid
    ) {

        BigDecimal totalPrice =
                sale.getTotalPrice();

        BigDecimal outstanding =
                totalPrice.subtract(totalPaid);

        SalePaymentStatus status;

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {

            status =
                    SalePaymentStatus.UNPAID;

        } else if (
                totalPaid.compareTo(totalPrice) < 0
        ) {

            status =
                    SalePaymentStatus.PARTIALLY_PAID;

        } else {

            status =
                    SalePaymentStatus.PAID;
        }

        return new SalePaymentSummaryResponse(
                sale.getId(),
                totalPrice,
                totalPaid,
                outstanding,
                status
        );
    }


    // ----------------------------------------------------
    // AUTHORIZATION
    // ----------------------------------------------------

    private void assertCanAccessSale(
            Sale sale,
            UUID actorUserId,
            boolean admin
    ) {

        if (admin) {
            return;
        }

        if (!sale.getSellerId().equals(actorUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another seller's sale"
            );
        }
    }
}