package com.muhammet.sales_service.sale.service;

import com.muhammet.sales_service.sale.domain.SaleStatus;
import com.muhammet.sales_service.sale.dto.response.SalesSummaryResponse;
import com.muhammet.sales_service.sale.repository.SaleRepository;

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
public class SalesSummaryService {

    private final SaleRepository saleRepository;

    @Value("${app.business.time-zone:Europe/Istanbul}")
    private String businessTimeZone;


    @Transactional(readOnly = true)
    public SalesSummaryResponse getSummary() {

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


        long todaySalesCount =
                saleRepository
                        .countBySoldAtGreaterThanEqualAndSoldAtLessThan(
                                from,
                                to
                        );


        BigDecimal todayRevenue =
                saleRepository
                        .sumRevenue(
                                from,
                                to,
                                SaleStatus.COMPLETED
                        );


        long pendingSales =
                saleRepository.countByStatus(
                        SaleStatus.PENDING_STOCK_UPDATE
                );

        long completedSales =
                saleRepository.countByStatus(
                        SaleStatus.COMPLETED
                );

        long failedSales =
                saleRepository.countByStatus(
                        SaleStatus.FAILED
                );


        return new SalesSummaryResponse(
                todaySalesCount,
                todayRevenue,
                pendingSales,
                completedSales,
                failedSales
        );
    }
}