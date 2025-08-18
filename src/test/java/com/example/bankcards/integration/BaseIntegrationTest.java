package com.example.bankcards.integration;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected CardRepository cardRepository;

    @Autowired
    protected CardStatusRepository cardStatusRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;

    protected User adminUser;
    protected User regularUser;
    protected Role adminRole;
    protected Role userRole;
    protected CardStatus activeStatus;
    protected CardStatus blockedStatus;
    protected CardStatus nonActiveStatus;
    protected Card testCard;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        setupTestData();
    }

    private void setupTestData() {
        // Создаем роли
        adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setAdmin(true);
        adminRole = roleRepository.save(adminRole);

        userRole = new Role();
        userRole.setName("USER");
        userRole.setAdmin(false);
        userRole = roleRepository.save(userRole);

        // Создаем статусы карт
        activeStatus = new CardStatus();
        activeStatus.setName("ACTIVE");
        activeStatus.setActive(true);
        activeStatus.setBlocked(false);
        activeStatus = cardStatusRepository.save(activeStatus);

        blockedStatus = new CardStatus();
        blockedStatus.setName("BLOCKED");
        blockedStatus.setActive(false);
        blockedStatus.setBlocked(true);
        blockedStatus = cardStatusRepository.save(blockedStatus);

        nonActiveStatus = new CardStatus();
        nonActiveStatus.setName("NONACTIVE");
        nonActiveStatus.setActive(false);
        nonActiveStatus.setBlocked(false);
        nonActiveStatus = cardStatusRepository.save(nonActiveStatus);

        // Создаем статус EXPIRED
        CardStatus expiredStatus = new CardStatus();
        expiredStatus.setName("EXPIRED");
        expiredStatus.setActive(false);
        expiredStatus.setBlocked(false);
        expiredStatus.setExpired(true);
        cardStatusRepository.save(expiredStatus);

        // Создаем пользователей
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setEmail("user@test.com");
        regularUser.setRole(userRole);
        regularUser = userRepository.save(regularUser);

        // Создаем тестовую карту
        testCard = new Card();
        testCard.setCardNumber("1234567890123456");
        testCard.setUser(regularUser);
        testCard.setStatus(activeStatus);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setHasBlockRequest(false);
        testCard = cardRepository.save(testCard);
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 
