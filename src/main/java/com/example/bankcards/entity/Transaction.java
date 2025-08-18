package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_from_id")
    @JsonBackReference
    private Card cardFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_to_id")
    @JsonBackReference
    private Card cardTo;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @Column(name = "currency", length = 3)
    private String currency = "RUB";

    // Конструкторы
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.currency = "RUB";
    }

    public Transaction(Card cardFrom, Card cardTo, BigDecimal amount, TransactionType transactionType, String description) {
        this();
        this.cardFrom = cardFrom;
        this.cardTo = cardTo;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.totalAmount = amount;
        this.referenceNumber = generateReferenceNumber();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getCardFrom() {
        return cardFrom;
    }

    public void setCardFrom(Card cardFrom) {
        this.cardFrom = cardFrom;
    }

    public Card getCardTo() {
        return cardTo;
    }

    public void setCardTo(Card cardTo) {
        this.cardTo = cardTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.totalAmount = amount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
        if (status == TransactionStatus.COMPLETED && this.processedDate == null) {
            this.processedDate = LocalDateTime.now();
        }
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Методы
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(status) || 
               TransactionStatus.DECLINED.equals(status) || 
               TransactionStatus.CANCELLED.equals(status);
    }

    public boolean isPending() {
        return TransactionStatus.PENDING.equals(status) || 
               TransactionStatus.PROCESSING.equals(status);
    }

    public boolean isTransfer() {
        return TransactionType.TRANSFER.equals(transactionType);
    }

    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }

    public String getFormattedTotalAmount() {
        return String.format("%.2f %s", totalAmount, currency);
    }

    public String getStatusDescription() {
        return status.getDescription();
    }

    public String getTransactionTypeDescription() {
        return transactionType.getDescription();
    }

    // Приватные методы
    private String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    @PrePersist
    protected void onCreate() {
        if (this.referenceNumber == null) {
            this.referenceNumber = generateReferenceNumber();
        }
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.totalAmount == null) {
            this.totalAmount = this.amount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.status == TransactionStatus.COMPLETED && this.processedDate == null) {
            this.processedDate = LocalDateTime.now();
        }
    }
} 