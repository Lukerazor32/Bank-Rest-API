package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserCardController userCardController;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Создаем тестовые данные
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("1234567890123456");
        testCard.setUser(testUser);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setHasBlockRequest(false);
    }

    @Test
    void testUserCardController_NotNull() {
        // Assert
        assertNotNull(userCardController);
        assertNotNull(cardService);
        assertNotNull(transactionService);
    }

    @Test
    void testBalanceResponse_Constructor() {
        // Arrange & Act
        UserCardController.BalanceResponse response = new UserCardController.BalanceResponse();
        response.setBalance(new BigDecimal("1000.00"));

        // Assert
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
    }

    @Test
    void testBlockRequestResponse_Constructor() {
        // Arrange & Act
        UserCardController.BlockRequestResponse response = new UserCardController.BlockRequestResponse(true, "Запрос создан");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Запрос создан", response.getMessage());
    }
} 