package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.stock.dto.PageResponse;
import com.muhammet.inventory_service.stock.dto.StockMovementResponse;

import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.entity.StockMovement;

import com.muhammet.inventory_service.stock.enums.StockMovementType;

import com.muhammet.inventory_service.stock.repository.StockItemRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;
import com.muhammet.inventory_service.stock.specification.StockMovementSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockItemRepository stockItemRepository;


    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> findAll(
            StockMovementType type,
            UUID stockItemId,
            Instant from,
            Instant to,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );

        validateDateRange(
                from,
                to
        );


        Specification<StockMovement> specification =
                StockMovementSpecification
                        .hasType(type)
                        .and(
                                StockMovementSpecification
                                        .hasStockItemId(stockItemId)
                        )
                        .and(
                                StockMovementSpecification
                                        .occurredFrom(from)
                        )
                        .and(
                                StockMovementSpecification
                                        .occurredTo(to)
                        );


        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        Page<StockMovement> result =
                stockMovementRepository.findAll(
                        specification,
                        pageable
                );


        return new PageResponse<>(
                result.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                result.getNumber(),
                result.getSize(),

                result.getTotalElements(),
                result.getTotalPages(),

                result.isFirst(),
                result.isLast()
        );
    }


    private StockMovementResponse toResponse(
            StockMovement movement
    ) {

        StockItem stockItem =
                movement.getStockItem();


        return new StockMovementResponse(
                movement.getId(),

                stockItem.getId(),
                stockItem.getName(),
                stockItem.getSku(),
                stockItem.getUnit(),

                movement.getMovementType(),

                movement.getQuantityChange(),
                movement.getPreviousOnHandQuantity(),
                movement.getNewOnHandQuantity(),

                movement.getReferenceType(),
                movement.getReferenceId(),

                movement.getReasonCode(),
                movement.getNote(),

                movement.getSourceOccurredAt(),
                movement.getCreatedAt()
        );
    }


    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page değeri negatif olamaz"
            );
        }

        if (size < 1 || size > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size değeri 1 ile 100 arasında olmalıdır"
            );
        }
    }


    private void validateDateRange(
            Instant from,
            Instant to
    ) {

        if (from != null &&
                to != null &&
                from.isAfter(to)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From tarihi to tarihinden sonra olamaz"
            );
        }
    }
    @Transactional(readOnly = true)
    public StockMovementResponse findById(
            UUID id
    ) {

        StockMovement movement =
                stockMovementRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Stok hareketi bulunamadı: " + id
                                )
                        );

        return toResponse(movement);
    }
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> findByStockItem(
            UUID stockItemId,
            StockMovementType type,
            Instant from,
            Instant to,
            int page,
            int size
    ) {

        if (!stockItemRepository.existsById(stockItemId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Stok kalemi bulunamadı: " + stockItemId
            );
        }

        return findAll(
                type,
                stockItemId,
                from,
                to,
                page,
                size
        );
    }
}