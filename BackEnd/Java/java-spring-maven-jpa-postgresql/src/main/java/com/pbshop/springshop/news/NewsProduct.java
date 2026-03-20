package com.pbshop.springshop.news;

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
@Table(name = "news_products")
public class NewsProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    public Long getId() { return id; }
    public News getNews() { return news; }
    public void setNews(News news) { this.news = news; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
