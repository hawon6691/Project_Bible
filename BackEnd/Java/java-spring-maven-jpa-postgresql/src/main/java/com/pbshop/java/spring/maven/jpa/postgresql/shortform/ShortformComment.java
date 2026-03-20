package com.pbshop.java.spring.maven.jpa.postgresql.shortform;

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
@Table(name = "shortform_comments")
public class ShortformComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortform_id", nullable = false)
    private Shortform shortform;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(columnDefinition = "CLOB", nullable = false)
    private String content;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    public Long getId() { return id; }
    public Shortform getShortform() { return shortform; }
    public void setShortform(Shortform shortform) { this.shortform = shortform; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
