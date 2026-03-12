package com.pbshop.springshop.shortform;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.pbshop.springshop.product.Product;

@Entity
@Table(name = "shortform_products")
public class ShortformProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortform_id", nullable = false)
    private Shortform shortform;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    public Long getId() { return id; }
    public Shortform getShortform() { return shortform; }
    public void setShortform(Shortform shortform) { this.shortform = shortform; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
