package com.example.bankcards.integration;

import com.example.bankcards.BankApplication;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BankApplication.class)
@ActiveProfiles("test")
@Transactional
class SimpleIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardStatusRepository cardStatusRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;
    private CardStatus activeStatus;
    private CardStatus blockedStatus;
    private CardStatus nonActiveStatus;
    private Card testCard;

    @BeforeEach
    void setUp() {
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

    @Test
    void testCreateCard() {
        
        Card createdCard = cardService.createCard(
            "9999888877776666",
            regularUser.getUsername(),
            LocalDate.now().plusYears(2),
            new BigDecimal("1000.00")
        );

        
        assertNotNull(createdCard);
        assertEquals("9999888877776666", createdCard.getCardNumber());
        assertEquals(regularUser.getUsername(), createdCard.getUser().getUsername());
        assertEquals(new BigDecimal("1000.00"), createdCard.getBalance());
        assertEquals("NONACTIVE", createdCard.getStatus().getName());
        assertFalse(createdCard.getStatus().isActive());
    }

    @Test
    void testCreateBlockRequest() {
        // Arrange
        String reason = "Потерял карту";

        
        cardService.createBlockRequest(testCard.getCardNumber(), reason);

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertTrue(updatedCard.isHasBlockRequest());
        assertEquals(reason, updatedCard.getBlockRequestReason());
        assertNotNull(updatedCard.getBlockRequestDate());
    }

    @Test
    void testConfirmBlockRequest() {
        // Arrange
        cardService.createBlockRequest(testCard.getCardNumber(), "Потерял карту");

        
        cardService.confirmBlockRequest(testCard.getCardNumber());

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertFalse(updatedCard.isHasBlockRequest());
        assertEquals(blockedStatus, updatedCard.getStatus());
    }

    @Test
    void testRejectBlockRequest() {
        // Arrange
        cardService.createBlockRequest(testCard.getCardNumber(), "Потерял карту");

        
        cardService.rejectBlockRequest(testCard.getCardNumber());

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertFalse(updatedCard.isHasBlockRequest());
        assertEquals(activeStatus, updatedCard.getStatus());
    }

    @Test
    void testGetCardsWithFilters() {
        // Arrange
        // Создаем еще одну карту для тестирования фильтров
        Card secondCard = new Card();
        secondCard.setCardNumber("5555666677778888");
        secondCard.setUser(regularUser);
        secondCard.setStatus(activeStatus);
        secondCard.setBalance(new BigDecimal("500.00"));
        secondCard.setExpirationDate(LocalDate.now().plusYears(1));
        secondCard.setCreatedAt(LocalDateTime.now());
        secondCard.setHasBlockRequest(false);
        cardRepository.save(secondCard);

        
        Page<Card> cardsPage = cardService.getCardsWithFilters(
                "user", "ACTIVE", "1234", false, 0, 10, "cardNumber", "asc"
        );

        
        assertNotNull(cardsPage);
        assertTrue(cardsPage.getTotalElements() > 0);
        assertTrue(cardsPage.getContent().stream()
                .anyMatch(card -> card.getCardNumber().contains("1234")));
    }

    @Test
    void testGetCardsWithBlockRequests() {
        // Arrange
        cardService.createBlockRequest(testCard.getCardNumber(), "Потерял карту");

        
        Page<Card> cardsPage = cardService.getCardsWithFilters(
                null, null, null, true, 0, 10, "cardNumber", "asc"
        );

        
        assertNotNull(cardsPage);
        assertTrue(cardsPage.getTotalElements() > 0);
        assertTrue(cardsPage.getContent().stream()
                .allMatch(Card::isHasBlockRequest));
    }

    @Test
    void testActivateCard() {
        // Arrange
        testCard.setStatus(blockedStatus);
        cardRepository.save(testCard);

        
        cardService.activateCard(testCard.getCardNumber());

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertEquals(activeStatus, updatedCard.getStatus());
    }

    @Test
    void testBlockCard() {
        
        cardService.blockCard(testCard.getCardNumber());

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertEquals(blockedStatus, updatedCard.getStatus());
    }

    @Test
    void testDeactivateCard() {
        
        cardService.deactivateCard(testCard.getCardNumber());

        
        Card updatedCard = cardRepository.findByCardNumber(testCard.getCardNumber()).orElse(null);
        assertNotNull(updatedCard);
        assertEquals(nonActiveStatus, updatedCard.getStatus());
    }

    @Test
    void testGetCardStatistics() {
        
        var statistics = cardService.getCardStatistics();

        
        assertNotNull(statistics);
        assertTrue(statistics.getTotalCards() > 0);
        assertTrue(statistics.getActiveCards() >= 0);
        assertTrue(statistics.getBlockedCards() >= 0);
        assertTrue(statistics.getNonActiveCards() >= 0);
    }

    @Test
    void testHasBlockRequest() {
        cardService.createBlockRequest(testCard.getCardNumber(), "Потерял карту");
        assertTrue(cardService.hasBlockRequest(testCard.getCardNumber()));
    }

    @Test
    void testGetCardById() {
        
        var foundCardOpt = cardService.getCardById(testCard.getId());

        
        assertTrue(foundCardOpt.isPresent());
        Card foundCard = foundCardOpt.get();
        assertEquals(testCard.getId(), foundCard.getId());
        assertEquals(testCard.getCardNumber(), foundCard.getCardNumber());
    }

    @Test
    void testGetCardByNumber() {
        
        var foundCardOpt = cardService.getCardByNumber(testCard.getCardNumber());

        
        assertTrue(foundCardOpt.isPresent());
        Card foundCard = foundCardOpt.get();
        assertEquals(testCard.getCardNumber(), foundCard.getCardNumber());
    }
} 
