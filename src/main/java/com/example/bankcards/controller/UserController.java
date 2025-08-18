package com.example.bankcards.controller;

import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Управление пользователями", description = "API для управления пользователями системы")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Получить профиль пользователя",
        description = "Получение профиля пользователя по имени пользователя"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Профиль пользователя получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден"
        )
    })
    public ResponseEntity<UserResponseDTO> getCurrentUserProfile(
            @Parameter(description = "Имя пользователя", required = true)
            @RequestParam String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserResponseDTO userDTO = UserResponseDTO.fromUser(user);
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить всех пользователей",
        description = "Получение списка всех пользователей системы (только для администраторов)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список пользователей получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
            .map(UserResponseDTO::fromUser)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить администраторов",
        description = "Получение списка всех пользователей с ролью ADMIN"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список администраторов получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<List<UserResponseDTO>> getAdminUsers() {
        List<User> users = userService.findAdminUsers();
        List<UserResponseDTO> userDTOs = users.stream()
            .map(UserResponseDTO::fromUser)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/regular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить обычных пользователей",
        description = "Получение списка всех пользователей с ролью USER"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список обычных пользователей получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<List<User>> getRegularUsers() {
        List<User> users = userService.findRegularUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Создать пользователя",
        description = "Создание нового пользователя в системе"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно создан",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при создании пользователя"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<User> createUser(
            @Parameter(description = "Данные для создания пользователя", required = true)
            @RequestBody CreateUserRequest request) {
        try {
            User newUser = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRoleName()
            );
            newUser.setPassword(null);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Обновить пользователя",
        description = "Обновление данных существующего пользователя"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно обновлен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при обновлении пользователя"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Данные для обновления", required = true)
            @RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateUser(
                userId,
                request.getUsername(),
                request.getEmail(),
                request.getRoleName()
            );
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(
        summary = "Изменить пароль",
        description = "Изменение пароля пользователя (админ может изменить любой, пользователь только свой)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пароль успешно изменен",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Успешное изменение",
                    value = "Пароль успешно изменен"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при изменении пароля"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен"
        )
    })
    public ResponseEntity<String> changePassword(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Новый пароль", required = true)
            @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userId, request.getNewPassword());
            return ResponseEntity.ok("Пароль успешно изменен");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Ошибка при изменении пароля: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Удалить пользователя",
        description = "Удаление пользователя из системы"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно удален",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Успешное удаление",
                    value = "Пользователь успешно удален"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при удалении пользователя"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("Пользователь успешно удален");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }

    public static class CreateUserRequest {
        private String username;
        private String password;
        private String email;
        private String roleName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    public static class UpdateUserRequest {
        private String username;
        private String email;
        private String roleName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    public static class ChangePasswordRequest {
        private String newPassword;

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
} 