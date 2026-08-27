package com.muhammet.purchase_service.purchase.repository;

import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase , Long> , JpaSpecificationExecutor<Purchase> {

    List<Purchase> findAllByStatusOrderByCreatedAtDesc(PurchaseStatus status);

    List<Purchase> findAllByStockItemIdOrderByCreatedAtDesc(UUID stockItemId);
}
