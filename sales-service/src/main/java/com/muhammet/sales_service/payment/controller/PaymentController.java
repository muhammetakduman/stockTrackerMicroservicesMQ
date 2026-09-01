package com.muhammet.sales_service.payment.controller;

import com.muhammet.sales_service.payment.dto.request.CreatePaymentRequest;
import com.muhammet.sales_service.payment.dto.response.PaymentOperationResponse;
import com.muhammet.sales_service.payment.dto.response.PaymentResponse;
import com.muhammet.sales_service.payment.dto.response.SalePaymentSummaryResponse;
import com.muhammet.sales_service.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/{saleId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SALES_USER')"
    )
    public PaymentOperationResponse recordPayment(
            @PathVariable Long saleId,
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {

        UUID actorUserId =
                UUID.fromString(jwt.getSubject());

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        return paymentService.recordPayment(
                saleId,
                request,
                actorUserId,
                admin
        );
    }


    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SALES_USER')"
    )
    public List<PaymentResponse> findPayments(
            @PathVariable Long saleId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {

        UUID actorUserId =
                UUID.fromString(jwt.getSubject());

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        return paymentService.findPaymentsBySale(
                saleId,
                actorUserId,
                admin
        );
    }


    @GetMapping("/summary")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SALES_USER')"
    )
    public SalePaymentSummaryResponse getSummary(
            @PathVariable Long saleId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {

        UUID actorUserId =
                UUID.fromString(jwt.getSubject());

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        return paymentService.getSalePaymentSummary(
                saleId,
                actorUserId,
                admin
        );
    }
}