package com.pbshop.java.spring.maven.jpa.postgresql.automotive;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoLeaseOfferRepository extends JpaRepository<AutoLeaseOffer, Long> {
    @EntityGraph(attributePaths = {"autoModel"})
    List<AutoLeaseOffer> findByAutoModelIdOrderByMonthlyPaymentAsc(Long autoModelId);
}
