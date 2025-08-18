package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private User testUser;
    private Card testCard;
    private CardStatus testStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Создаем тестовые данные
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testStatus = new CardStatus();
        testStatus.setId(1L);
        testStatus.setName("ACTIVE");
        testStatus.setActive(true);
        testStatus.setBlocked(false);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("1234567890123456");
        testCard.setUser(testUser);
        testCard.setStatus(testStatus);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setHasBlockRequest(false);
    }

    @Test
    void testCardController_NotNull() {
        // Assert
        assertNotNull(cardController);
        assertNotNull(cardService);
    }

    @Test
    void testCreateCardRequest_Constructor() {
        // Arrange & Act
        CardController.CreateCardRequest request = new CardController.CreateCardRequest();
        request.setCardNumber("1234567890123456");
        request.setUsername("testuser");
        request.setExpirationDate(LocalDate.now().plusYears(2));
        request.setInitialBalance(new BigDecimal("1000.00"));

        // Assert
        assertEquals("1234567890123456", request.getCardNumber());
        assertEquals("testuser", request.getUsername());
        assertNotNull(request.getExpirationDate());
        assertEquals(new BigDecimal("1000.00"), request.getInitialBalance());
    }

    @Test
    void testUpdateCardRequest_Constructor() {
        // Arrange & Act
        CardController.UpdateCardRequest request = new CardController.UpdateCardRequest();
        request.setUsername("testuser");
        request.setExpirationDate(LocalDate.now().plusYears(2));
        request.setBalance(new BigDecimal("1000.00"));

        // Assert
        assertEquals("testuser", request.getUsername());
        assertNotNull(request.getExpirationDate());
        assertEquals(new BigDecimal("1000.00"), request.getBalance());
    }
} 