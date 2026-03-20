package com.pbshop.springshop.trust;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustScoreHistoryRepository extends JpaRepository<TrustScoreHistory, Long> {

    @EntityGraph(attributePaths = {"seller"})
    Optional<TrustScoreHistory> findFirstBySellerIdOrderByRecordedAtDescIdDesc(Long sellerId);

    @EntityGraph(attributePaths = {"seller"})
    List<TrustScoreHistory> findBySellerIdOrderByRecordedAtDescIdDesc(Long sellerId);
}
