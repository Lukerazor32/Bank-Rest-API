package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Card;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO для вывода информации о пользователе
 * Содержит только необходимые для отображения поля
 */
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String roleName;
    private boolean isAdmin;
    private List<CardResponseDTO> cards;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username, String email, String roleName, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roleName = roleName;
        this.isAdmin = isAdmin;
    }

    public static UserResponseDTO fromUser(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleName(user.getRoleName());
        dto.setAdmin(user.isAdmin());
        
        if (user.getCards() != null && !user.getCards().isEmpty()) {
            dto.setCards(user.getCards().stream()
                .map(CardResponseDTO::fromCard)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<CardResponseDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardResponseDTO> cards) {
        this.cards = cards;
    }
} 