package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameWithRole(username);
    }

    public Optional<User> findByUsernameWithCards(String username) {
        return userRepository.findByUsernameWithRoleAndCards(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    public List<User> findAdminUsers() {
        return userRepository.findByRoleIsAdminTrue();
    }

    public List<User> findRegularUsers() {
        return userRepository.findByRoleIsAdminFalse();
    }

    public User createUser(String username, String password, String email, String roleName) {
        // Проверяем, что пользователь не существует
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с именем " + username + " уже существует");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует");
        }

        // Находим роль
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роль " + roleName + " не найдена"));

        // Создаем пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);

        return userRepository.save(user);
    }

    public User updateUser(Long userId, String username, String email, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        // Проверяем уникальность username
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с именем " + username + " уже существует");
        }

        // Проверяем уникальность email
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует");
        }

        // Находим роль
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роль " + roleName + " не найдена"));

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);

        return userRepository.save(user);
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    public boolean authenticateUser(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }

    public long countUsersByRole(String roleName) {
        return userRepository.countByRoleName(roleName);
    }

    public boolean isUserAdmin(String username) {
        Optional<User> userOpt = userRepository.findByUsernameWithRole(username);
        return userOpt.map(User::isAdmin).orElse(false);
    }
} 