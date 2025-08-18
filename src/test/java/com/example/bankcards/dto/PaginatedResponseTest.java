package com.example.bankcards.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedResponseTest {

    @Test
    void testFromPage_Success() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

        // Act
        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testFromPage_EmptyPage() {
        // Arrange
        List<String> content = Arrays.asList();
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 0);

        // Act
        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testFromPage_LastPage() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2");
        Page<String> page = new PageImpl<>(content, PageRequest.of(2, 10), 22);

        // Act
        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(2, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(22, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testFromPage_MiddlePage() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 10), 25);

        // Act
        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(1, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testConstructor() {
        // Act
        PaginatedResponse<String> response = new PaginatedResponse<>();

        // Assert
        assertNotNull(response);
        assertNull(response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(0, response.getPageSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PaginatedResponse<String> response = new PaginatedResponse<>();
        List<String> content = Arrays.asList("item1", "item2");

        // Act
        response.setContent(content);
        response.setPageNumber(1);
        response.setPageSize(5);
        response.setTotalElements(10);
        response.setTotalPages(2);
        response.setHasNext(true);
        response.setHasPrevious(true);
        response.setFirst(false);
        response.setLast(false);

        // Assert
        assertEquals(content, response.getContent());
        assertEquals(1, response.getPageNumber());
        assertEquals(5, response.getPageSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testWithCardResponseDTO() {
        // Arrange
        CardResponseDTO card1 = new CardResponseDTO();
        card1.setId(1L);
        card1.setCardNumber("1234567890123456");

        CardResponseDTO card2 = new CardResponseDTO();
        card2.setId(2L);
        card2.setCardNumber("6543210987654321");

        List<CardResponseDTO> content = Arrays.asList(card1, card2);
        Page<CardResponseDTO> page = new PageImpl<>(content, PageRequest.of(0, 10), 15);

        // Act
        PaginatedResponse<CardResponseDTO> response = PaginatedResponse.fromPage(page);

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(2, response.getContent().size());
        assertEquals(1L, response.getContent().get(0).getId());
        assertEquals(2L, response.getContent().get(1).getId());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(15, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }
} 