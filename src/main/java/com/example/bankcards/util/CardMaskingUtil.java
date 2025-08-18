package com.example.bankcards.util;

/**
 * Утилита для маскирования номеров банковских карт
 * Обеспечивает безопасность и соответствие стандартам PCI DSS
 */
public class CardMaskingUtil {

    private static final char MASK_CHAR = '*';
    
    private static final int VISIBLE_START = 4;
    private static final int VISIBLE_END = 4;

    /**
     * Маскирует номер карты, оставляя видимыми только первые и последние 4 цифры
     * 
     * @param cardNumber полный номер карты
     * @return маскированный номер карты
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return cardNumber;
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < 8) {
            return cardNumber;
        }

        StringBuilder masked = new StringBuilder();
        
        masked.append(cleanNumber.substring(0, VISIBLE_START));
        
        for (int i = VISIBLE_START; i < cleanNumber.length() - VISIBLE_END; i++) {
            masked.append(MASK_CHAR);
        }
        
        masked.append(cleanNumber.substring(cleanNumber.length() - VISIBLE_END));
        
        return formatCardNumber(masked.toString());
    }

    /**
     * Маскирует номер карты с указанием количества видимых символов
     * 
     * @param cardNumber полный номер карты
     * @param visibleStart количество видимых символов в начале
     * @param visibleEnd количество видимых символов в конце
     * @return маскированный номер карты
     */
    public static String maskCardNumber(String cardNumber, int visibleStart, int visibleEnd) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return cardNumber;
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < visibleStart + visibleEnd) {
            return cardNumber;
        }

        StringBuilder masked = new StringBuilder();
        
        masked.append(cleanNumber.substring(0, visibleStart));
        
        for (int i = visibleStart; i < cleanNumber.length() - visibleEnd; i++) {
            masked.append(MASK_CHAR);
        }
        
        masked.append(cleanNumber.substring(cleanNumber.length() - visibleEnd));
        
        return formatCardNumber(masked.toString());
    }

    /**
     * Полностью маскирует номер карты (оставляет только последние 4 цифры)
     * 
     * @param cardNumber полный номер карты
     * @return полностью маскированный номер карты
     */
    public static String fullyMaskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return cardNumber;
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < 4) {
            return cardNumber;
        }

        StringBuilder masked = new StringBuilder();
        
        for (int i = 0; i < cleanNumber.length() - 4; i++) {
            masked.append(MASK_CHAR);
        }
        
        masked.append(cleanNumber.substring(cleanNumber.length() - 4));
        
        return formatCardNumber(masked.toString());
    }

    /**
     * Форматирует номер карты, добавляя дефисы для читаемости
     * 
     * @param cardNumber номер карты (только цифры)
     * @return отформатированный номер карты
     */
    private static String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }

        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append('-');
            }
            formatted.append(cardNumber.charAt(i));
        }
        
        return formatted.toString();
    }

    /**
     * Проверяет, является ли номер карты маскированным
     * 
     * @param cardNumber номер карты для проверки
     * @return true, если номер маскированный
     */
    public static boolean isMasked(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        return cardNumber.contains(String.valueOf(MASK_CHAR));
    }

    /**
     * Получает последние 4 цифры номера карты
     * 
     * @param cardNumber номер карты (может быть маскированным)
     * @return последние 4 цифры
     */
    public static String getLastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < 4) {
            return cleanNumber;
        }
        
        return cleanNumber.substring(cleanNumber.length() - 4);
    }

    /**
     * Получает первые 4 цифры номера карты
     * 
     * @param cardNumber номер карты (может быть маскированным)
     * @return первые 4 цифры
     */
    public static String getFirstFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() < 4) {
            return cleanNumber;
        }
        
        return cleanNumber.substring(0, 4);
    }
} 