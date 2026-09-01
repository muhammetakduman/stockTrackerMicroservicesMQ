package com.muhammet.sales_service.payment.controller;

import com.muhammet.sales_service.payment.dto.request.VoidPaymentRequest;
import com.muhammet.sales_service.payment.dto.response.PaymentOperationResponse;
import com.muhammet.sales_service.payment.dto.response.PaymentResponse;
import com.muhammet.sales_service.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;


    @GetMapping
    public Page<PaymentResponse> findAll(
            Pageable pageable
    ) {

        return paymentService
                .findAllPayments(pageable);
    }


    @GetMapping("/{paymentId}")
    public PaymentResponse findById(
            @PathVariable UUID paymentId
    ) {

        return paymentService
                .findPaymentById(paymentId);
    }


    @PostMapping("/{paymentId}/void")
    public PaymentOperationResponse voidPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody VoidPaymentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID adminUserId =
                UUID.fromString(jwt.getSubject());

        return paymentService.voidPayment(
                paymentId,
                request.reason(),
                adminUserId
        );
    }
}