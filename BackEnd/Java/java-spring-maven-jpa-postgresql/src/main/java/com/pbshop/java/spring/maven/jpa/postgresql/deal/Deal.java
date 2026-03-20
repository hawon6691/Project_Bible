package com.pbshop.java.spring.maven.jpa.postgresql.deal;

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

import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;

@Entity
@Table(name = "deals")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type = "SPECIAL";

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(name = "deal_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal dealPrice;

    @Column(name = "discount_rate", nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private int stock;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDealPrice() { return dealPrice; }
    public void setDealPrice(BigDecimal dealPrice) { this.dealPrice = dealPrice; }
    public int getDiscountRate() { return discountRate; }
    public void setDiscountRate(int discountRate) { this.discountRate = discountRate; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getStartAt() { return startAt; }
    public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }
    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }
}
