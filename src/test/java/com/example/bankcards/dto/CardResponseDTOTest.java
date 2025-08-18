package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CardResponseDTOTest {

    private User testUser;
    private Card testCard;
    private CardStatus activeStatus;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
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
    void testFromCard_Success() {
        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals(testCard.getId(), dto.getId());
        assertEquals(testCard.getCardNumber(), dto.getCardNumber());
        assertEquals("1234-****-****-3456", dto.getMaskedCardNumber());
        assertEquals(testCard.getStatusName(), dto.getStatus());
        assertEquals(testCard.getStatusDescription(), dto.getStatusDescription());
        assertEquals(testUser.getUsername(), dto.getUsername());
        assertEquals(testCard.getExpirationDate().toString(), dto.getExpirationDate());
        assertEquals(testCard.getBalance().toString(), dto.getBalance());
        assertEquals(testCard.isHasBlockRequest(), dto.isHasBlockRequest());
        assertEquals("", dto.getBlockRequestReason());
        assertEquals("", dto.getBlockRequestDate());
    }

    @Test
    void testFromCardWithFullMasking_Success() {
        // Act
        CardResponseDTO dto = CardResponseDTO.fromCardWithFullMasking(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals(testCard.getId(), dto.getId());
        assertEquals(testCard.getCardNumber(), dto.getCardNumber());
        assertEquals("****-****-****-3456", dto.getMaskedCardNumber());
        assertEquals(testCard.getStatusName(), dto.getStatus());
        assertEquals(testCard.getStatusDescription(), dto.getStatusDescription());
        assertEquals(testUser.getUsername(), dto.getUsername());
        assertEquals(testCard.getExpirationDate().toString(), dto.getExpirationDate());
        assertEquals(testCard.getBalance().toString(), dto.getBalance());
        assertEquals(testCard.isHasBlockRequest(), dto.isHasBlockRequest());
        assertEquals("", dto.getBlockRequestReason());
        assertEquals("", dto.getBlockRequestDate());
    }

    @Test
    void testFromCard_WithBlockRequest() {
        // Arrange
        testCard.setHasBlockRequest(true);
        testCard.setBlockRequestReason("Потерял карту");
        testCard.setBlockRequestDate(LocalDateTime.now());

        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertTrue(dto.isHasBlockRequest());
        assertEquals("Потерял карту", dto.getBlockRequestReason());
        assertNotNull(dto.getBlockRequestDate());
        assertFalse(dto.getBlockRequestDate().isEmpty());
    }

    @Test
    void testFromCard_NullUser() {
        // Arrange
        testCard.setUser(null);

        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals("Unknown", dto.getUsername());
    }

    @Test
    void testFromCard_NullExpirationDate() {
        // Arrange
        testCard.setExpirationDate(null);

        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals("", dto.getExpirationDate());
    }

    @Test
    void testFromCard_NullBalance() {
        // Arrange
        testCard.setBalance(null);

        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals("0.00", dto.getBalance());
    }

    @Test
    void testFromCard_NullStatus() {
        // Arrange
        testCard.setStatus(null);

        // Act
        CardResponseDTO dto = CardResponseDTO.fromCard(testCard);

        // Assert
        assertNotNull(dto);
        assertEquals("UNKNOWN", dto.getStatus());
        assertEquals("Неизвестный статус", dto.getStatusDescription());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        CardResponseDTO dto = new CardResponseDTO();

        // Act
        dto.setId(1L);
        dto.setCardNumber("1234567890123456");
        dto.setMaskedCardNumber("1234-****-****-3456");
        dto.setStatus("ACTIVE");
        dto.setStatusDescription("Активная карта");
        dto.setUsername("testuser");
        dto.setExpirationDate("2025-12-31");
        dto.setBalance("1000.00");
        dto.setHasBlockRequest(true);
        dto.setBlockRequestReason("Потерял карту");
        dto.setBlockRequestDate("2024-01-01T10:00:00");

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("1234567890123456", dto.getCardNumber());
        assertEquals("1234-****-****-3456", dto.getMaskedCardNumber());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("Активная карта", dto.getStatusDescription());
        assertEquals("testuser", dto.getUsername());
        assertEquals("2025-12-31", dto.getExpirationDate());
        assertEquals("1000.00", dto.getBalance());
        assertTrue(dto.isHasBlockRequest());
        assertEquals("Потерял карту", dto.getBlockRequestReason());
        assertEquals("2024-01-01T10:00:00", dto.getBlockRequestDate());
    }

    @Test
    void testConstructor() {
        // Act
        CardResponseDTO dto = new CardResponseDTO(
            1L, "1234567890123456", "ACTIVE", "Активная карта",
            "testuser", "2025-12-31", "1000.00"
        );

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("1234567890123456", dto.getCardNumber());
        assertEquals("1234-****-****-3456", dto.getMaskedCardNumber());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("Активная карта", dto.getStatusDescription());
        assertEquals("testuser", dto.getUsername());
        assertEquals("2025-12-31", dto.getExpirationDate());
        assertEquals("1000.00", dto.getBalance());
    }
} 