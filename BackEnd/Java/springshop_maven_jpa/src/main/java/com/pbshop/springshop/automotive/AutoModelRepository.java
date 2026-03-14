package com.pbshop.springshop.automotive;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoModelRepository extends JpaRepository<AutoModel, Long> {
    List<AutoModel> findByBrandContainingIgnoreCaseAndTypeContainingIgnoreCaseOrderByIdAsc(String brand, String type);
}
