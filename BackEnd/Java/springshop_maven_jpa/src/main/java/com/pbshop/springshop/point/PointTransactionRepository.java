package com.pbshop.springshop.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Page<PointTransaction> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Page<PointTransaction> findByUserIdAndTypeOrderByIdDesc(Long userId, String type, Pageable pageable);
}
