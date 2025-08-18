package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardStatusService cardStatusService;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CardStatus activeStatus;
    private CardStatus blockedStatus;
    private CardStatus nonActiveStatus;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setAdmin(false);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(userRole);

        activeStatus = new CardStatus();
        activeStatus.setId(1L);
        activeStatus.setName("ACTIVE");
        activeStatus.setActive(true);
        activeStatus.setBlocked(false);

        blockedStatus = new CardStatus();
        blockedStatus.setId(2L);
        blockedStatus.setName("BLOCKED");
        blockedStatus.setActive(false);
        blockedStatus.setBlocked(true);

        nonActiveStatus = new CardStatus();
        nonActiveStatus.setId(3L);
        nonActiveStatus.setName("NON_ACTIVE");
        nonActiveStatus.setActive(false);
        nonActiveStatus.setBlocked(false);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("1234567890123456");
        testCard.setUser(testUser);
        testCard.setStatus(activeStatus);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setHasBlockRequest(false);
    }

    @Test
    void testCreateCard_Success() {
        String cardNumber = "1234567890123456";
        String username = "testuser";
        LocalDate expirationDate = LocalDate.now().plusYears(2);
        BigDecimal balance = new BigDecimal("1000.00");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(cardStatusService.getNonActiveStatus()).thenReturn(nonActiveStatus);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        Card result = cardService.createCard(cardNumber, username, expirationDate, balance);

        assertNotNull(result);
        assertEquals(cardNumber, result.getCardNumber());
        assertEquals(testUser, result.getUser());
        assertEquals(expirationDate, result.getExpirationDate());
        assertEquals(nonActiveStatus, result.getStatus());
        assertEquals(balance, result.getBalance());
        assertFalse(result.getStatus().isActive());

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void testCreateCard_UserNotFound() {
        String cardNumber = "1234567890123456";
        String username = "nonexistent";
        LocalDate expirationDate = LocalDate.now().plusYears(2);
        BigDecimal balance = new BigDecimal("1000.00");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createCard(cardNumber, username, expirationDate, balance);
        });

        assertEquals("Пользователь с именем nonexistent не найден", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateCard_AdminUser() {
        String cardNumber = "1234567890123456";
        String username = "admin";
        LocalDate expirationDate = LocalDate.now().plusYears(2);
        BigDecimal balance = new BigDecimal("1000.00");

        User adminUser = new User();
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setAdmin(true);
        adminUser.setUsername("admin");
        adminUser.setRole(adminRole);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createCard(cardNumber, username, expirationDate, balance);
        });

        assertEquals("Нельзя создавать карты для администраторов по соображениям безопасности", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        
        String cardNumber = "1234567890123456";
        String username = "testuser";
        LocalDate expirationDate = LocalDate.now().plusYears(2);
        BigDecimal balance = new BigDecimal("1000.00");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByCardNumber(cardNumber)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createCard(cardNumber, username, expirationDate, balance);
        });

        assertEquals("Карта с номером 1234567890123456 уже существует", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(cardRepository).existsByCardNumber(cardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateBlockRequest_Success() {
        
        String cardNumber = "1234567890123456";
        String reason = "Потерял карту";

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        
        Card result = cardService.createBlockRequest(cardNumber, reason);

        
        assertNotNull(result);
        assertTrue(result.isHasBlockRequest());
        assertEquals(reason, result.getBlockRequestReason());
        assertNotNull(result.getBlockRequestDate());

        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository).save(testCard);
    }

    @Test
    void testCreateBlockRequest_CardNotFound() {
        
        String cardNumber = "9999999999999999";
        String reason = "Потерял карту";

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createBlockRequest(cardNumber, reason);
        });

        assertEquals("Карта с номером 9999999999999999 не найдена", exception.getMessage());
        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateBlockRequest_CardNotActive() {
        
        String cardNumber = "1234567890123456";
        String reason = "Потерял карту";

        testCard.setStatus(blockedStatus);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createBlockRequest(cardNumber, reason);
        });

        assertEquals("Карта уже заблокирована или неактивна", exception.getMessage());
        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateBlockRequest_RequestAlreadyExists() {
        
        String cardNumber = "1234567890123456";
        String reason = "Потерял карту";

        testCard.setHasBlockRequest(true);
        testCard.setBlockRequestReason("Предыдущая причина");

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.createBlockRequest(cardNumber, reason);
        });

        assertEquals("Запрос на блокировку карты уже существует", exception.getMessage());
        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testConfirmBlockRequest_Success() {
        
        String cardNumber = "1234567890123456";

        testCard.setHasBlockRequest(true);
        testCard.setBlockRequestReason("Потерял карту");
        testCard.setBlockRequestDate(LocalDateTime.now());

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));
        when(cardStatusService.getBlockedStatus()).thenReturn(blockedStatus);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        
        Card result = cardService.confirmBlockRequest(cardNumber);

        
        assertNotNull(result);
        assertEquals(blockedStatus, result.getStatus());
        assertFalse(result.isHasBlockRequest());
        assertNull(result.getBlockRequestReason());
        assertNull(result.getBlockRequestDate());

        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardStatusService).getBlockedStatus();
        verify(cardRepository).save(testCard);
    }

    @Test
    void testConfirmBlockRequest_NoRequestExists() {
        
        String cardNumber = "1234567890123456";

        testCard.setHasBlockRequest(false);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.confirmBlockRequest(cardNumber);
        });

        assertEquals("Запрос на блокировку карты не найден", exception.getMessage());
        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testRejectBlockRequest_Success() {
        
        String cardNumber = "1234567890123456";

        testCard.setHasBlockRequest(true);
        testCard.setBlockRequestReason("Потерял карту");
        testCard.setBlockRequestDate(LocalDateTime.now());

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        
        Card result = cardService.rejectBlockRequest(cardNumber);

        
        assertNotNull(result);
        assertEquals(activeStatus, result.getStatus()); // Статус остается прежним
        assertFalse(result.isHasBlockRequest());
        assertNull(result.getBlockRequestReason());
        assertNull(result.getBlockRequestDate());

        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository).save(testCard);
    }

    @Test
    void testGetCardsWithFilters_AllCards() {
        
        List<Card> cards = Arrays.asList(testCard);
        Page<Card> cardPage = new PageImpl<>(cards, PageRequest.of(0, 10), 1);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        
        Page<Card> result = cardService.getCardsWithFilters(
            null, null, null, false, 0, 10, "id", "desc"
        );

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testCard, result.getContent().get(0));

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetCardsWithFilters_ByUsername() {
        
        List<Card> cards = Arrays.asList(testCard);
        Page<Card> cardPage = new PageImpl<>(cards, PageRequest.of(0, 10), 1);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        
        Page<Card> result = cardService.getCardsWithFilters(
            "testuser", null, null, false, 0, 10, "id", "desc"
        );

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetCardsWithFilters_ByStatus() {
        
        List<Card> cards = Arrays.asList(testCard);
        Page<Card> cardPage = new PageImpl<>(cards, PageRequest.of(0, 10), 1);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        
        Page<Card> result = cardService.getCardsWithFilters(
            null, "ACTIVE", null, false, 0, 10, "id", "desc"
        );

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetCardsWithFilters_WithBlockRequests() {
        
        List<Card> cards = Arrays.asList(testCard);
        Page<Card> cardPage = new PageImpl<>(cards, PageRequest.of(0, 10), 1);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        
        Page<Card> result = cardService.getCardsWithFilters(
            null, null, null, true, 0, 10, "id", "desc"
        );

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testHasBlockRequest_True() {
        
        String cardNumber = "1234567890123456";
        testCard.setHasBlockRequest(true);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        
        boolean result = cardService.hasBlockRequest(cardNumber);

        
        assertTrue(result);
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @Test
    void testHasBlockRequest_False() {
        
        String cardNumber = "1234567890123456";
        testCard.setHasBlockRequest(false);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        
        boolean result = cardService.hasBlockRequest(cardNumber);

        
        assertFalse(result);
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @Test
    void testHasBlockRequest_CardNotFound() {
        
        String cardNumber = "9999999999999999";

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.hasBlockRequest(cardNumber);
        });

        assertEquals("Карта с номером 9999999999999999 не найдена", exception.getMessage());
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @Test
    void testGetCardStatistics() {
        
        when(cardRepository.count()).thenReturn(100L);
        when(cardRepository.countByStatus(activeStatus)).thenReturn(60L);
        when(cardRepository.countByStatus(blockedStatus)).thenReturn(20L);
        when(cardRepository.countByStatus(nonActiveStatus)).thenReturn(15L);

        when(cardStatusService.getActiveStatus()).thenReturn(activeStatus);
        when(cardStatusService.getBlockedStatus()).thenReturn(blockedStatus);
        when(cardStatusService.getNonActiveStatus()).thenReturn(nonActiveStatus);

        
        CardService.CardStatistics stats = cardService.getCardStatistics();

        
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalCards());
        assertEquals(60L, stats.getActiveCards());
        assertEquals(20L, stats.getBlockedCards());
        assertEquals(15L, stats.getNonActiveCards());
        assertEquals(60.0, stats.getActivePercentage(), 0.01);

        verify(cardRepository).count();
        verify(cardRepository).countByStatus(activeStatus);
        verify(cardRepository).countByStatus(blockedStatus);
        verify(cardRepository).countByStatus(nonActiveStatus);
    }
} 
