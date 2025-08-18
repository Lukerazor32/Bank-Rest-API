package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RefreshTokenRequest;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                
                // Генерируем access token
                String accessToken = jwtUtil.generateAccessToken(userDetails);
                
                // Создаем и сохраняем refresh token
                RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(userDetails.getUsername());
                
                // Возвращаем оба токена
                return new AuthResponse(
                    accessToken, 
                    refreshTokenEntity.getToken(), 
                    userDetails.getUsername(),
                    jwtUtil.getAccessTokenExpirationInSeconds()
                );
            } else {
                return new AuthResponse("Аутентификация не удалась");
            }
        } catch (Exception e) {
            return new AuthResponse("Ошибка аутентификации: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            System.out.println("AuthService: Начинаем обновление токена для refresh token: " + request.getRefreshToken());
            
            // Проверяем refresh token в базе данных
            RefreshToken refreshTokenEntity = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token не найден"));

            System.out.println("AuthService: Refresh token найден в БД для пользователя: " + refreshTokenEntity.getUsername());

            // Проверяем валидность refresh token
            refreshTokenService.verifyExpiration(refreshTokenEntity);

            // Получаем пользователя
            String username = refreshTokenEntity.getUsername();
            System.out.println("AuthService: Генерируем новый access token для пользователя: " + username);
            
            // Генерируем новый access token
            String newAccessToken = jwtUtil.generateAccessToken(username);
            
            // Создаем новый refresh token (старый автоматически отзывается)
            System.out.println("AuthService: Создаем новый refresh token");
            RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(username);
            
            // Отзываем старый refresh token
            System.out.println("AuthService: Отзываем старый refresh token");
            boolean revoked = refreshTokenService.revokeToken(request.getRefreshToken());
            System.out.println("AuthService: Старый refresh token отозван: " + revoked);
            
            return new AuthResponse(
                newAccessToken, 
                newRefreshTokenEntity.getToken(), 
                username,
                jwtUtil.getAccessTokenExpirationInSeconds()
            );
        } catch (Exception e) {
            System.err.println("AuthService: Ошибка при обновлении токена: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse("Ошибка обновления токена: " + e.getMessage());
        }
    }

    public AuthResponse validateToken(String token) {
        try {
            if (jwtUtil.validateAccessToken(token)) {
                String username = jwtUtil.extractUsername(token);
                return new AuthResponse("Access токен валиден для пользователя: " + username);
            } else {
                return new AuthResponse("Access токен недействителен");
            }
        } catch (Exception e) {
            return new AuthResponse("Ошибка валидации токена: " + e.getMessage());
        }
    }

    public AuthResponse logout(String refreshToken) {
        try {
            refreshTokenService.revokeToken(refreshToken);
            return new AuthResponse("Выход выполнен успешно");
        } catch (Exception e) {
            return new AuthResponse("Ошибка при выходе: " + e.getMessage());
        }
    }

    public String extractUsernameFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
} 