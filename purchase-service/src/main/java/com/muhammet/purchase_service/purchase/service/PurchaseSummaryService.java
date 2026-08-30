package com.muhammet.purchase_service.purchase.service;

import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;
import com.muhammet.purchase_service.purchase.dto.response.PurchaseSummaryResponse;
import com.muhammet.purchase_service.purchase.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PurchaseSummaryService {

    private final PurchaseRepository purchaseRepository;

    @Value("${app.business.time-zone:Europe/Istanbul}")
    private String businessTimeZone;


    @Transactional(readOnly = true)
    public PurchaseSummaryResponse getSummary() {

        ZoneId zoneId =
                ZoneId.of(
                        businessTimeZone
                );

        LocalDate today =
                LocalDate.now(
                        zoneId
                );

        Instant from =
                today
                        .atStartOfDay(zoneId)
                        .toInstant();

        Instant to =
                today
                        .plusDays(1)
                        .atStartOfDay(zoneId)
                        .toInstant();


        long todayPurchaseCount =
                purchaseRepository
                        .countByPurchasedAtGreaterThanEqualAndPurchasedAtLessThan(
                                from,
                                to
                        );


        BigDecimal todayPurchaseAmount =
                purchaseRepository
                        .sumPurchaseAmount(
                                from,
                                to,
                                PurchaseStatus.COMPLETED
                        );


        long pendingPurchases =
                purchaseRepository.countByStatus(
                        PurchaseStatus.PENDING_STOCK_UPDATE
                );

        long completedPurchases =
                purchaseRepository.countByStatus(
                        PurchaseStatus.COMPLETED
                );

        long failedPurchases =
                purchaseRepository.countByStatus(
                        PurchaseStatus.FAILED
                );

        long cancelledPurchases =
                purchaseRepository.countByStatus(
                        PurchaseStatus.CANCELLED
                );


        return new PurchaseSummaryResponse(
                todayPurchaseCount,
                todayPurchaseAmount,
                pendingPurchases,
                completedPurchases,
                failedPurchases,
                cancelledPurchases
        );
    }
}