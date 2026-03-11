package com.pbshop.springshop.push;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.pbshop.springshop.user.User;

@Entity
@Table(name = "push_preferences")
public class PushPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "marketing_enabled", nullable = false)
    private boolean marketingEnabled = true;

    @Column(name = "order_enabled", nullable = false)
    private boolean orderEnabled = true;

    @Column(name = "chat_enabled", nullable = false)
    private boolean chatEnabled = true;

    @Column(name = "deal_enabled", nullable = false)
    private boolean dealEnabled = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isMarketingEnabled() {
        return marketingEnabled;
    }

    public void setMarketingEnabled(boolean marketingEnabled) {
        this.marketingEnabled = marketingEnabled;
    }

    public boolean isOrderEnabled() {
        return orderEnabled;
    }

    public void setOrderEnabled(boolean orderEnabled) {
        this.orderEnabled = orderEnabled;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public boolean isDealEnabled() {
        return dealEnabled;
    }

    public void setDealEnabled(boolean dealEnabled) {
        this.dealEnabled = dealEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
