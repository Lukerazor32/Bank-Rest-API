package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для отображения пользователя без циклических ссылок
 * Используется когда нужно вернуть User entity напрямую
 */
@JsonIgnoreProperties({"role.users", "cards"})
public class UserViewDTO extends User {
    

    
    @Override
    public Role getRole() {
        if (super.getRole() != null) {
            Role role = new Role();
            role.setId(super.getRole().getId());
            role.setName(super.getRole().getName());
            role.setAdmin(super.getRole().isAdmin());
            return role;
        }
        return null;
    }
} 