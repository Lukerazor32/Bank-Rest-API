package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private CardStatus status;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "has_block_request", nullable = false)
    private boolean hasBlockRequest = false;

    @Column(name = "block_request_reason")
    private String blockRequestReason;

    @Column(name = "block_request_date")
    private LocalDateTime blockRequestDate;

    public Card() {
        this.createdAt = LocalDateTime.now();
    }

    public Card(String cardNumber, User user, LocalDate expirationDate, CardStatus status, BigDecimal balance) {
        this();
        this.cardNumber = cardNumber;
        this.user = user;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHasBlockRequest() {
        return hasBlockRequest;
    }

    public void setHasBlockRequest(boolean hasBlockRequest) {
        this.hasBlockRequest = hasBlockRequest;
    }

    public String getBlockRequestReason() {
        return blockRequestReason;
    }

    public void setBlockRequestReason(String blockRequestReason) {
        this.blockRequestReason = blockRequestReason;
    }

    public LocalDateTime getBlockRequestDate() {
        return blockRequestDate;
    }

    public void setBlockRequestDate(LocalDateTime blockRequestDate) {
        this.blockRequestDate = blockRequestDate;
    }

    public boolean isActive() {
        return status != null && status.isCardActive();
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate) || (status != null && status.isCardExpired());
    }

    public boolean isBlocked() {
        return status != null && status.isCardBlocked();
    }

    public boolean isUsable() {
        return status != null && status.isCardUsable() && !LocalDate.now().isAfter(expirationDate);
    }

    public String getStatusName() {
        return status != null ? status.getName() : "UNKNOWN";
    }

    public String getStatusDescription() {
        return status != null ? status.getDescription() : "Неизвестный статус";
    }

    /**
     * Создать запрос на блокировку карты
     */
    public void createBlockRequest(String reason) {
        this.hasBlockRequest = true;
        this.blockRequestReason = reason;
        this.blockRequestDate = LocalDateTime.now();
    }

    /**
     * Отменить запрос на блокировку карты
     */
    public void cancelBlockRequest() {
        this.hasBlockRequest = false;
        this.blockRequestReason = null;
        this.blockRequestDate = null;
    }
} 