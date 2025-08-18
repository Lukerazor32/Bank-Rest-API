package com.example.bankcards.entity;

public enum TransactionType {
    TRANSFER("transfer", "Перевод между картами");

    private final String value;
    private final String description;

    TransactionType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionType fromString(String text) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    @Override
    public String toString() {
        return this.value;
    }
} 