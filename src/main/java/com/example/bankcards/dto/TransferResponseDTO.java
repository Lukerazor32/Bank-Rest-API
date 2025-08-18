package com.example.bankcards.dto;

import com.example.bankcards.entity.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponseDTO {
    private Long transactionId;
    private String cardFromNumber;
    private String cardToNumber;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private String message;

    // Конструкторы
    public TransferResponseDTO() {}

    public TransferResponseDTO(Long transactionId, String cardFromNumber, String cardToNumber, 
                              BigDecimal amount, String description, TransactionStatus status, 
                              LocalDateTime transactionDate, String message) {
        this.transactionId = transactionId;
        this.cardFromNumber = cardFromNumber;
        this.cardToNumber = cardToNumber;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.transactionDate = transactionDate;
        this.message = message;
    }

    // Статические методы для создания ответов
    public static TransferResponseDTO success(Long transactionId, String cardFromNumber, String cardToNumber,
                                             BigDecimal amount, String description, LocalDateTime transactionDate) {
        return new TransferResponseDTO(transactionId, cardFromNumber, cardToNumber,
                                      amount, description, TransactionStatus.COMPLETED,
                                      transactionDate, "Перевод выполнен успешно");
    }

    public static TransferResponseDTO failure(String cardFromNumber, String cardToNumber,
                                             BigDecimal amount, String description, String message) {
        return new TransferResponseDTO(null, cardFromNumber, cardToNumber,
                                      amount, description, TransactionStatus.FAILED,
                                      LocalDateTime.now(), message);
    }

    // Геттеры и сеттеры
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardFromNumber() {
        return cardFromNumber;
    }

    public void setCardFromNumber(String cardFromNumber) {
        this.cardFromNumber = cardFromNumber;
    }

    public String getCardToNumber() {
        return cardToNumber;
    }

    public void setCardToNumber(String cardToNumber) {
        this.cardToNumber = cardToNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 