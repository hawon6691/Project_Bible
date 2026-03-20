package com.pbshop.java.spring.maven.jpa.postgresql.inquiry;

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
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;

@Entity
@Table(name = "product_inquiries")
public class ProductInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String content;

    @Column(name = "is_secret", nullable = false)
    private boolean secret;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "answer_content", columnDefinition = "CLOB")
    private String answerContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by_user_id")
    private User answeredBy;

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public User getAnsweredBy() {
        return answeredBy;
    }

    public void setAnsweredBy(User answeredBy) {
        this.answeredBy = answeredBy;
    }

    public OffsetDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(OffsetDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
