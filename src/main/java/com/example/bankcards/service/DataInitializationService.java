package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Value("${app.data.initialization.enabled:true}")
    private boolean initializationEnabled;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardStatusService cardStatusService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!initializationEnabled) {
            System.out.println("Инициализация данных отключена");
            return;
        }
        
        System.out.println("Инициализация данных...");
        
        initializeRoles();
        
        initializeUsers();
        
        initializeCards();
        
        System.out.println("Инициализация данных завершена");
    }

    private void initializeRoles() {
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setAdmin(true);
            roleRepository.save(adminRole);
            System.out.println("Роль ADMIN создана");
        }

        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setAdmin(false);
            roleRepository.save(userRole);
            System.out.println("Роль USER создана");
        }
    }

    private void initializeUsers() {
        if (!userRepository.existsByUsername("admin")) {
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            if (adminRole.isPresent()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@bank.com");
                admin.setRole(adminRole.get());
                userRepository.save(admin);
                System.out.println("Администратор admin создан");
            }
        }

        if (!userRepository.existsByUsername("user")) {
            Optional<Role> userRole = roleRepository.findByName("USER");
            if (userRole.isPresent()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEmail("user@bank.com");
                user.setRole(userRole.get());
                userRepository.save(user);
                System.out.println("Пользователь user создан");
            }
        }

        if (!userRepository.existsByUsername("manager")) {
            Optional<Role> userRole = roleRepository.findByName("USER");
            if (userRole.isPresent()) {
                User manager = new User();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager@bank.com");
                manager.setRole(userRole.get());
                userRepository.save(manager);
                System.out.println("Пользователь manager создан");
            }
        }
    }

    private void initializeCards() {
        createTestCards();
    }

    private void createTestCards() {
        Optional<User> userUser = userRepository.findByUsername("user");
        if (userUser.isPresent()) {
            if (!cardRepository.existsByCardNumber("2222-2222-2222-2222")) {
                Card userCard1 = new Card();
                userCard1.setCardNumber("2222-2222-2222-2222");
                userCard1.setUser(userUser.get());
                userCard1.setExpirationDate(LocalDate.now().plusYears(2));
                userCard1.setStatus(cardStatusService.getActiveStatus());
                userCard1.setBalance(new BigDecimal("5000.00"));
                cardRepository.save(userCard1);
                System.out.println("Основная карта для user создана");
            }

            if (!cardRepository.existsByCardNumber("3333-3333-3333-3333")) {
                Card userCard2 = new Card();
                userCard2.setCardNumber("3333-3333-3333-3333");
                userCard2.setUser(userUser.get());
                userCard2.setExpirationDate(LocalDate.now().plusYears(1));
                userCard2.setStatus(cardStatusService.getNonActiveStatus());
                userCard2.setBalance(new BigDecimal("1000.00"));
                cardRepository.save(userCard2);
                System.out.println("Дополнительная карта для user создана");
            }
        }

        Optional<User> managerUser = userRepository.findByUsername("manager");
        if (managerUser.isPresent()) {
            if (!cardRepository.existsByCardNumber("4444-4444-4444-4444")) {
                Card managerCard1 = new Card();
                managerCard1.setCardNumber("4444-4444-4444-4444");
                managerCard1.setUser(managerUser.get());
                managerCard1.setExpirationDate(LocalDate.now().plusYears(4));
                managerCard1.setStatus(cardStatusService.getActiveStatus());
                managerCard1.setBalance(new BigDecimal("7500.00"));
                cardRepository.save(managerCard1);
                System.out.println("Основная карта для manager создана");
            }

            if (!cardRepository.existsByCardNumber("5555-5555-5555-5555")) {
                Card managerCard2 = new Card();
                managerCard2.setCardNumber("5555-5555-5555-5555");
                managerCard2.setUser(managerUser.get());
                managerCard2.setExpirationDate(LocalDate.now().plusYears(2));
                managerCard2.setStatus(cardStatusService.getBlockedStatus());
                managerCard2.setBalance(new BigDecimal("2500.00"));
                cardRepository.save(managerCard2);
                System.out.println("Заблокированная карта для manager создана");
            }
        }
        
        System.out.println("Карты для администраторов не создаются по соображениям безопасности");
    }
} 