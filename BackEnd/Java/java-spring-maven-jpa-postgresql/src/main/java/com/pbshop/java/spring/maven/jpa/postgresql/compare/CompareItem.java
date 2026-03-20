package com.pbshop.java.spring.maven.jpa.postgresql.compare;

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
@Table(name = "compare_items")
public class CompareItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "compare_key", nullable = false)
    private String compareKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
    public Long getId() { return id; }
    public String getCompareKey() { return compareKey; }
    public void setCompareKey(String compareKey) { this.compareKey = compareKey; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
