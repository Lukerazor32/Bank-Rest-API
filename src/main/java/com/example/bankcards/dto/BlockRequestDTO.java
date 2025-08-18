package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO для запроса на блокировку карты")
public class BlockRequestDTO {

    @Schema(description = "ID карты")
    private Long cardId;

    @Schema(description = "Замаскированный номер карты")
    private String maskedCardNumber;

    @Schema(description = "Имя пользователя")
    private String username;

    @Schema(description = "Причина блокировки")
    private String reason;

    @Schema(description = "Дата запроса на блокировку")
    private LocalDateTime requestDate;

    @Schema(description = "Баланс карты")
    private String balance;

    @Schema(description = "Статус карты")
    private String status;

    public BlockRequestDTO() {}

    public BlockRequestDTO(Long cardId, String maskedCardNumber, String username, String reason, 
                          LocalDateTime requestDate, String balance, String status) {
        this.cardId = cardId;
        this.maskedCardNumber = maskedCardNumber;
        this.username = username;
        this.reason = reason;
        this.requestDate = requestDate;
        this.balance = balance;
        this.status = status;
    }

    public static BlockRequestDTO fromCard(Card card) {
        return new BlockRequestDTO(
            card.getId(),
            card.getCardNumber(),
            card.getUser().getUsername(),
            card.getBlockRequestReason(),
            card.getBlockRequestDate(),
            card.getBalance().toString(),
            card.getStatusName()
        );
    }

    // Геттеры и сеттеры
    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 