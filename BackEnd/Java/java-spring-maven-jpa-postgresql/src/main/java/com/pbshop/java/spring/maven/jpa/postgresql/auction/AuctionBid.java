package com.pbshop.java.spring.maven.jpa.postgresql.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.pbshop.java.spring.maven.jpa.postgresql.user.User;

@Entity
@Table(name = "auction_bids")
public class AuctionBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    @Column(columnDefinition = "CLOB")
    private String description;
    @Column(name = "delivery_days")
    private Integer deliveryDays;
    @Column(nullable = false)
    private String status = "ACTIVE";
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
    public Long getId() { return id; }
    public Auction getAuction() { return auction; }
    public void setAuction(Auction auction) { this.auction = auction; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
