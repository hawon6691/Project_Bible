package com.pbshop.springshop.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    @EntityGraph(attributePaths = {"user", "auction"})
    List<AuctionBid> findByAuctionIdOrderByIdAsc(Long auctionId);
    @EntityGraph(attributePaths = {"user", "auction"})
    Optional<AuctionBid> findByIdAndAuctionId(Long id, Long auctionId);
}
