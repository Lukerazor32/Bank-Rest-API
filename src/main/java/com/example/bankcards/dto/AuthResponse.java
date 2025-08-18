package com.example.bankcards.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String message;
    private long expiresIn; // время жизни access токена в секундах

    // Конструкторы
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String username, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.expiresIn = expiresIn;
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    // Геттеры и сеттеры
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    // Устаревшие методы для обратной совместимости
    @Deprecated
    public String getToken() {
        return accessToken;
    }

    @Deprecated
    public void setToken(String token) {
        this.accessToken = token;
    }
} 