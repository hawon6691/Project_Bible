package com.pbshop.java.spring.maven.jpa.postgresql.auction;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @EntityGraph(attributePaths = {"user", "category", "selectedBid"})
    List<Auction> findAllByOrderByIdDesc();
    @EntityGraph(attributePaths = {"user", "category", "selectedBid"})
    List<Auction> findByStatusOrderByIdDesc(String status);
    @EntityGraph(attributePaths = {"user", "category", "selectedBid"})
    List<Auction> findByCategoryIdOrderByIdDesc(Long categoryId);
    @EntityGraph(attributePaths = {"user", "category", "selectedBid"})
    List<Auction> findByStatusAndCategoryIdOrderByIdDesc(String status, Long categoryId);
}
