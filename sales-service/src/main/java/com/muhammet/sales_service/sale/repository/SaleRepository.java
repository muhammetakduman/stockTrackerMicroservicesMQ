package com.muhammet.sales_service.sale.repository;

import com.muhammet.sales_service.sale.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

}
