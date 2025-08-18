package com.example.bankcards.entity;

public enum TransactionStatus {
    PENDING("pending", "В обработке"),
    PROCESSING("processing", "Обрабатывается"),
    COMPLETED("completed", "Выполнена"),
    FAILED("failed", "Не выполнена"),
    CANCELLED("cancelled", "Отменена"),
    DECLINED("declined", "Отклонена");

    private final String value;
    private final String description;

    TransactionStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionStatus fromString(String text) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    @Override
    public String toString() {
        return this.value;
    }
} 