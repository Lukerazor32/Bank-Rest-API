package com.example.bankcards.integration;

import com.example.bankcards.entity.Card;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CardControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void testSearchCards_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/cards/search")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)));
    }

    @Test
    void testSearchCards_UserAccess_Forbidden() throws Exception {
        
        mockMvc.perform(get("/api/cards/search")
                .with(user("user").roles("USER"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSearchCards_Unauthorized() throws Exception {
        
        mockMvc.perform(get("/api/cards/search")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSearchCards_WithFilters() throws Exception {
        
        mockMvc.perform(get("/api/cards/search")
                .with(user("admin").roles("ADMIN"))
                .param("username", "user")
                .param("status", "ACTIVE")
                .param("search", "1234")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].cardNumber", containsString("1234")));
    }

    @Test
    void testGetCardsByUser_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/cards/user/user")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].username", is("user")));
    }

    @Test
    void testGetCardById_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/cards/{id}", testCard.getId())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCard.getId().intValue())))
                .andExpect(jsonPath("$.cardNumber", is(testCard.getCardNumber())))
                .andExpect(jsonPath("$.username", is("user")));
    }

    @Test
    void testGetCardById_NotFound() throws Exception {
        
        mockMvc.perform(get("/api/cards/999999")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCardByNumber_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/cards/number/{cardNumber}", testCard.getCardNumber())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber", is(testCard.getCardNumber())))
                .andExpect(jsonPath("$.username", is("user")));
    }

    @Test
    void testGetCardByNumber_NotFound() throws Exception {
        
        mockMvc.perform(get("/api/cards/number/9999999999999999")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBlockRequests_AdminAccess_Success() throws Exception {
        // Arrange - создаем карту с запросом на блокировку
        Card cardWithRequest = new Card();
        cardWithRequest.setCardNumber("6543210987654321");
        cardWithRequest.setUser(regularUser);
        cardWithRequest.setStatus(activeStatus);
        cardWithRequest.setBalance(new BigDecimal("500.00"));
        cardWithRequest.setExpirationDate(LocalDate.now().plusYears(1));
        cardWithRequest.setCreatedAt(LocalDateTime.now());
        cardWithRequest.setHasBlockRequest(true);
        cardWithRequest.setBlockRequestReason("Потерял карту");
        cardWithRequest.setBlockRequestDate(LocalDateTime.now());
        cardRepository.save(cardWithRequest);

        
        mockMvc.perform(get("/api/cards/block-requests")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void testCreateCard_AdminAccess_Success() throws Exception {
        // Arrange
        String createCardRequest = asJsonString(new CreateCardRequest(
                "1111222233334444",
                "user",
                LocalDate.now().plusYears(3),
                new BigDecimal("2000.00")
        ));

        
        mockMvc.perform(post("/api/cards/create")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCardRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber", is("1111222233334444")))
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.balance", is("2000.00")));
    }

    @Test
    void testCreateCard_InvalidData_BadRequest() throws Exception {
        // Arrange
        String invalidRequest = asJsonString(new CreateCardRequest(
                "", // пустой номер карты
                "user",
                LocalDate.now().plusYears(3),
                new BigDecimal("2000.00")
        ));

        
        mockMvc.perform(post("/api/cards/create")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCard_AdminAccess_Success() throws Exception {
        // Arrange
        String updateCardRequest = asJsonString(new UpdateCardRequest(
                testCard.getCardNumber(),
                "newuser",
                LocalDate.now().plusYears(4),
                new BigDecimal("3000.00")
        ));

        
        mockMvc.perform(put("/api/cards/{cardNumber}", testCard.getCardNumber())
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCardRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testActivateCard_AdminAccess_Success() throws Exception {
        // Arrange - создаем заблокированную карту
        Card blockedCard = new Card();
        blockedCard.setCardNumber("5555666677778888");
        blockedCard.setUser(regularUser);
        blockedCard.setStatus(blockedStatus);
        blockedCard.setBalance(new BigDecimal("100.00"));
        blockedCard.setExpirationDate(LocalDate.now().plusYears(1));
        blockedCard.setCreatedAt(LocalDateTime.now());
        blockedCard.setHasBlockRequest(false);
        blockedCard = cardRepository.save(blockedCard);

        
        mockMvc.perform(put("/api/cards/{cardNumber}/activate", blockedCard.getCardNumber())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void testBlockCard_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(put("/api/cards/{cardNumber}/block", testCard.getCardNumber())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("BLOCKED")));
    }

    @Test
    void testDeactivateCard_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(put("/api/cards/{cardNumber}/deactivate", testCard.getCardNumber())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("NONACTIVE")));
    }

    @Test
    void testDeleteCard_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(delete("/api/cards/{cardNumber}", testCard.getCardNumber())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCardStatistics_AdminAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/cards/statistics")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards", greaterThan(0)))
                .andExpect(jsonPath("$.activeCards", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.blockedCards", greaterThanOrEqualTo(0)));
    }

    // Вспомогательные классы для запросов
    private static class CreateCardRequest {
        private String cardNumber;
        private String username;
        private LocalDate expirationDate;
        private BigDecimal initialBalance;

        public CreateCardRequest(String cardNumber, String username, LocalDate expirationDate, BigDecimal initialBalance) {
            this.cardNumber = cardNumber;
            this.username = username;
            this.expirationDate = expirationDate;
            this.initialBalance = initialBalance;
        }

        // Геттеры
        public String getCardNumber() { return cardNumber; }
        public String getUsername() { return username; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public BigDecimal getInitialBalance() { return initialBalance; }
    }

    private static class UpdateCardRequest {
        private String cardNumber;
        private String username;
        private LocalDate expirationDate;
        private BigDecimal balance;

        public UpdateCardRequest(String cardNumber, String username, LocalDate expirationDate, BigDecimal balance) {
            this.cardNumber = cardNumber;
            this.username = username;
            this.expirationDate = expirationDate;
            this.balance = balance;
        }

        // Геттеры
        public String getCardNumber() { return cardNumber; }
        public String getUsername() { return username; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public BigDecimal getBalance() { return balance; }
    }
} 
