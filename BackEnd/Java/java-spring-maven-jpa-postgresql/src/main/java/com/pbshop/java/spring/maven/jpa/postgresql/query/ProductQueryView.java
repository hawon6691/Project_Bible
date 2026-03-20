package com.pbshop.java.spring.maven.jpa.postgresql.query;

import java.time.OffsetDateTime;

import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_query_views")
public class ProductQueryView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "search_keywords_json", columnDefinition = "CLOB")
    private String searchKeywordsJson;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getSearchKeywordsJson() {
        return searchKeywordsJson;
    }

    public void setSearchKeywordsJson(String searchKeywordsJson) {
        this.searchKeywordsJson = searchKeywordsJson;
    }
}
