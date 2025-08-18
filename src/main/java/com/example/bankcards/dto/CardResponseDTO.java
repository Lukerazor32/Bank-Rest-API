package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardMaskingUtil;

public class CardResponseDTO {
    private Long id;
    private String cardNumber;
    private String maskedCardNumber;
    private String status;
    private String statusDescription;
    private String username;
    private String expirationDate;
    private String balance;
    private boolean hasBlockRequest;
    private String blockRequestReason;
    private String blockRequestDate;

    public CardResponseDTO() {}

    public CardResponseDTO(Long id, String cardNumber, String status, String statusDescription, 
                          String username, String expirationDate, String balance) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.maskedCardNumber = CardMaskingUtil.maskCardNumber(cardNumber);
        this.status = status;
        this.statusDescription = statusDescription;
        this.username = username;
        this.expirationDate = expirationDate;
        this.balance = balance;
    }

    public static CardResponseDTO fromCard(Card card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setMaskedCardNumber(CardMaskingUtil.maskCardNumber(card.getCardNumber()));
        dto.setStatus(card.getStatusName());
        dto.setStatusDescription(card.getStatusDescription());
        dto.setUsername(card.getUser() != null ? card.getUser().getUsername() : "Unknown");
        dto.setExpirationDate(card.getExpirationDate() != null ? card.getExpirationDate().toString() : "");
        dto.setBalance(card.getBalance() != null ? card.getBalance().toString() : "0.00");
        dto.setHasBlockRequest(card.isHasBlockRequest());
        dto.setBlockRequestReason(card.getBlockRequestReason() != null ? card.getBlockRequestReason() : "");
        dto.setBlockRequestDate(card.getBlockRequestDate() != null ? card.getBlockRequestDate().toString() : "");
        return dto;
    }

    public static CardResponseDTO fromCardWithFullMasking(Card card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setMaskedCardNumber(CardMaskingUtil.fullyMaskCardNumber(card.getCardNumber()));
        dto.setStatus(card.getStatusName());
        dto.setStatusDescription(card.getStatusDescription());
        dto.setUsername(card.getUser() != null ? card.getUser().getUsername() : "Unknown");
        dto.setExpirationDate(card.getExpirationDate() != null ? card.getExpirationDate().toString() : "");
        dto.setBalance(card.getBalance() != null ? card.getBalance().toString() : "0.00");
        dto.setHasBlockRequest(card.isHasBlockRequest());
        dto.setBlockRequestReason(card.getBlockRequestReason() != null ? card.getBlockRequestReason() : "");
        dto.setBlockRequestDate(card.getBlockRequestDate() != null ? card.getBlockRequestDate().toString() : "");
        return dto;
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
        this.maskedCardNumber = CardMaskingUtil.maskCardNumber(cardNumber);
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
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

    public String getBlockRequestDate() {
        return blockRequestDate;
    }

    public void setBlockRequestDate(String blockRequestDate) {
        this.blockRequestDate = blockRequestDate;
    }
} 