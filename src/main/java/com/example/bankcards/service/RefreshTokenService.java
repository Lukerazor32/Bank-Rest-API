package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration:604800}")
    private Long refreshTokenExpiration;

    public RefreshToken createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();
        
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiration);
        
        System.out.println("RefreshTokenService: Создаем refresh token для пользователя: " + username);
        System.out.println("RefreshTokenService: Время истечения: " + expiryDate);
        System.out.println("RefreshTokenService: Длительность в секундах: " + refreshTokenExpiration);
        
        RefreshToken refreshToken = new RefreshToken(token, username, expiryDate);
        
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        System.out.println("RefreshTokenService: Проверяем refresh token: " + token.getToken());
        System.out.println("RefreshTokenService: Время истечения: " + token.getExpiryDate());
        System.out.println("RefreshTokenService: Текущее время: " + LocalDateTime.now());
        System.out.println("RefreshTokenService: Истек: " + token.isExpired());
        System.out.println("RefreshTokenService: Отозван: " + token.isRevoked());
        
        if (token.isExpired()) {
            System.out.println("RefreshTokenService: Токен истек, удаляем");
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token истек: " + token.getToken());
        }
        
        if (token.isRevoked()) {
            System.out.println("RefreshTokenService: Токен отозван");
            throw new RuntimeException("Refresh token отозван: " + token.getToken());
        }
        
        System.out.println("RefreshTokenService: Токен валиден, отмечаем как использованный");
        token.markAsUsed();
        return refreshTokenRepository.save(token);
    }

    public boolean revokeToken(String token) {
        System.out.println("RefreshTokenService: Отзываем refresh token: " + token);
        int updatedRows = refreshTokenRepository.revokeToken(token);
        boolean success = updatedRows > 0;
        System.out.println("RefreshTokenService: Токен отозван: " + success + " (обновлено строк: " + updatedRows + ")");
        return success;
    }

    public void revokeAllTokensByUsername(String username) {
        refreshTokenRepository.revokeAllTokensByUsername(username);
    }

    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
    }

    public boolean isTokenValid(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().isValid();
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        deleteExpiredTokens();
    }
} 