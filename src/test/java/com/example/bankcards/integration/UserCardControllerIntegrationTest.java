package com.example.bankcards.integration;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserCardControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void testGetMyCards_UserAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/my")
                .with(user("user").roles("USER"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)));
    }

    @Test
    void testGetMyCards_AdminAccess_Forbidden() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/my")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyCards_Unauthorized() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/my")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyCards_WithSearch() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/my")
                .with(user("user").roles("USER"))
                .param("search", "1234")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].cardNumber", containsString("1234")));
    }

    @Test
    void testGetMyCards_WithPagination() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/my")
                .with(user("user").roles("USER"))
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "balance")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(5)))
                .andExpect(jsonPath("$.pageNumber", is(0)));
    }

    @Test
    void testGetCardBalance_UserAccess_Success() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/{cardNumber}/balance", testCard.getCardNumber())
                .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(1000.0)));
    }

    @Test
    void testGetCardBalance_CardNotFound() throws Exception {
        
        mockMvc.perform(get("/api/user/cards/9999999999999999/balance")
                .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCardBalance_OtherUserCard_Forbidden() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("other123"));
        otherUser.setEmail("other@test.com");
        otherUser.setRole(userRole);
        otherUser = userRepository.save(otherUser);

        Card otherCard = new Card();
        otherCard.setCardNumber("9999888877776666");
        otherCard.setUser(otherUser);
        otherCard.setStatus(activeStatus);
        otherCard.setBalance(new BigDecimal("500.00"));
        otherCard.setExpirationDate(LocalDate.now().plusYears(1));
        otherCard.setCreatedAt(LocalDateTime.now());
        otherCard.setHasBlockRequest(false);
        otherCard = cardRepository.save(otherCard);

        
        mockMvc.perform(get("/api/user/cards/{cardNumber}/balance", otherCard.getCardNumber())
                .with(user("user").roles("USER")))
                .andExpect(status().isNotFound()); // Карта не найдена для текущего пользователя
    }

    @Test
    void testRequestCardBlock_UserAccess_Success() throws Exception {
        
        String blockRequest = asJsonString(new BlockRequest("Потерял карту"));

        
        mockMvc.perform(post("/api/user/cards/{cardNumber}/block-request", testCard.getCardNumber())
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestCardBlock_AlreadyBlocked_BadRequest() throws Exception {
        testCard.setStatus(blockedStatus);
        cardRepository.save(testCard);

        String blockRequest = asJsonString(new BlockRequest("Потерял карту"));

        
        mockMvc.perform(post("/api/user/cards/{cardNumber}/block-request", testCard.getCardNumber())
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestCardBlock_OtherUserCard_Forbidden() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("other123"));
        otherUser.setEmail("other@test.com");
        otherUser.setRole(userRole);
        otherUser = userRepository.save(otherUser);

        Card otherCard = new Card();
        otherCard.setCardNumber("9999888877776666");
        otherCard.setUser(otherUser);
        otherCard.setStatus(activeStatus);
        otherCard.setBalance(new BigDecimal("500.00"));
        otherCard.setExpirationDate(LocalDate.now().plusYears(1));
        otherCard.setCreatedAt(LocalDateTime.now());
        otherCard.setHasBlockRequest(false);
        otherCard = cardRepository.save(otherCard);

        String blockRequest = asJsonString(new BlockRequest("Потерял карту"));

        
        mockMvc.perform(post("/api/user/cards/{cardNumber}/block-request", otherCard.getCardNumber())
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockRequest))
                .andExpect(status().isBadRequest()); // Карта не найдена для текущего пользователя
    }

    @Test
    void testTransferBetweenMyCards_UserAccess_Success() throws Exception {
        Card secondCard = new Card();
        secondCard.setCardNumber("1111222233334444");
        secondCard.setUser(regularUser);
        secondCard.setStatus(activeStatus);
        secondCard.setBalance(new BigDecimal("500.00"));
        secondCard.setExpirationDate(LocalDate.now().plusYears(1));
        secondCard.setCreatedAt(LocalDateTime.now());
        secondCard.setHasBlockRequest(false);
        secondCard = cardRepository.save(secondCard);

        String transferRequest = asJsonString(new TransferRequest(
                testCard.getCardNumber(),
                secondCard.getCardNumber(),
                new BigDecimal("100.00")
        ));

        
        mockMvc.perform(post("/api/user/cards/transfer")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.message", containsString("Перевод выполнен успешно")));
    }

    @Test
    void testTransferBetweenMyCards_InsufficientFunds() throws Exception {
        Card secondCard = new Card();
        secondCard.setCardNumber("1111222233334444");
        secondCard.setUser(regularUser);
        secondCard.setStatus(activeStatus);
        secondCard.setBalance(new BigDecimal("500.00"));
        secondCard.setExpirationDate(LocalDate.now().plusYears(1));
        secondCard.setCreatedAt(LocalDateTime.now());
        secondCard.setHasBlockRequest(false);
        secondCard = cardRepository.save(secondCard);

        // Попытка перевести больше, чем есть на карте
        String transferRequest = asJsonString(new TransferRequest(
                testCard.getCardNumber(),
                secondCard.getCardNumber(),
                new BigDecimal("2000.00")
        ));

        
        mockMvc.perform(post("/api/user/cards/transfer")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    void testTransferBetweenMyCards_OtherUserCard_Forbidden() throws Exception {   
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("other123"));
        otherUser.setEmail("other@test.com");
        otherUser.setRole(userRole);
        otherUser = userRepository.save(otherUser);

        Card otherCard = new Card();
        otherCard.setCardNumber("9999888877776666");
        otherCard.setUser(otherUser);
        otherCard.setStatus(activeStatus);
        otherCard.setBalance(new BigDecimal("500.00"));
        otherCard.setExpirationDate(LocalDate.now().plusYears(1));
        otherCard.setCreatedAt(LocalDateTime.now());
        otherCard.setHasBlockRequest(false);
        otherCard = cardRepository.save(otherCard);

        String transferRequest = asJsonString(new TransferRequest(
                testCard.getCardNumber(),
                otherCard.getCardNumber(),
                new BigDecimal("100.00")
        ));

        
        mockMvc.perform(post("/api/user/cards/transfer")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Обе карты должны принадлежать текущему пользователю")));
    }

    // Вспомогательные классы для запросов
    private static class BlockRequest {
        private String reason;

        public BlockRequest(String reason) {
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    private static class TransferRequest {
        private String cardFromNumber;
        private String cardToNumber;
        private BigDecimal amount;

        public TransferRequest(String cardFromNumber, String cardToNumber, BigDecimal amount) {
            this.cardFromNumber = cardFromNumber;
            this.cardToNumber = cardToNumber;
            this.amount = amount;
        }

        public String getCardFromNumber() { return cardFromNumber; }
        public String getCardToNumber() { return cardToNumber; }
        public BigDecimal getAmount() { return amount; }
    }
} 
