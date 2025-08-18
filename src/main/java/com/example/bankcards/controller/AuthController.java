package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Аутентификация", description = "API для аутентификации и управления токенами")
public class AuthController {

    @Autowired
    private com.example.bankcards.service.AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Аутентификация пользователя и получение JWT токенов"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Успешная аутентификация",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Успешный вход",
                    value = """
                    {
                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                        "type": "Bearer",
                        "username": "admin",
                        "expiresIn": 900
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка аутентификации",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Ошибка входа",
                    value = """
                    {
                        "message": "Ошибка аутентификации: Неверные учетные данные"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Данные для входа", required = true)
            @Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);

        if (response.getAccessToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Обновление токена",
        description = "Обновление access токена с помощью refresh токена"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Токены успешно обновлены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка обновления токена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        )
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Refresh токен", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);

        if (response.getAccessToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Выход из системы",
        description = "Отзыв refresh токена и выход пользователя"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Успешный выход",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при выходе",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        )
    })
    public ResponseEntity<AuthResponse> logout(
            @Parameter(description = "Refresh токен для отзыва", required = true)
            @RequestHeader("Refresh-Token") String refreshToken) {
        if (refreshToken != null) {
            AuthResponse response = authService.logout(refreshToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(new AuthResponse("Отсутствует refresh токен"));
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Проверка состояния",
        description = "Проверка работоспособности сервиса аутентификации"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Сервис работает",
        content = @Content(
            mediaType = "text/plain",
            examples = @ExampleObject(
                name = "Статус сервиса",
                value = "JWT сервис работает с access и refresh токенами"
            )
        )
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("JWT сервис работает с access и refresh токенами");
    }
} 