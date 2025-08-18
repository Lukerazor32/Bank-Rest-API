package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO для запроса на перевод денег между картами
 */
public class TransferRequestDTO {
    
    @NotBlank(message = "Номер карты отправителя обязателен")
    @Size(min = 16, max = 19, message = "Номер карты должен содержать от 16 до 19 символов")
    private String cardFromNumber;
    
    @NotBlank(message = "Номер карты получателя обязателен")
    @Size(min = 16, max = 19, message = "Номер карты должен содержать от 16 до 19 символов")
    private String cardToNumber;
    
    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Описание не должно превышать 255 символов")
    private String description;
    
    // Конструкторы
    public TransferRequestDTO() {}
    
    public TransferRequestDTO(String cardFromNumber, String cardToNumber, BigDecimal amount, String description) {
        this.cardFromNumber = cardFromNumber;
        this.cardToNumber = cardToNumber;
        this.amount = amount;
        this.description = description;
    }
    
    // Геттеры и сеттеры
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
} 