package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.CardMaskingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import com.example.bankcards.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/user/cards")
@CrossOrigin(origins = "*")
@Tag(name = "Пользователь - Управление картами", description = "API для пользователей по управлению своими картами")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Получить все карты текущего пользователя с пагинацией
     */
    @GetMapping("/my")
    @Operation(
        summary = "Получить мои карты",
        description = "Получение списка карт текущего пользователя с пагинацией и поиском"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список карт пользователя получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaginatedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль USER"
        )
    })
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getMyCards(
            @Parameter(description = "Поиск по номеру карты") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы (начиная с 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки (id, cardNumber, balance, expirationDate, createdAt)") 
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc, desc)") 
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String username = getCurrentUsername();
        
        Page<Card> cardsPage = cardService.getCardsWithFilters(
            username, null, search, false, page, size, sortBy, sortDir
        );
        
        Page<CardResponseDTO> responsePage = cardsPage.map(CardResponseDTO::fromCard);
        return ResponseEntity.ok(PaginatedResponse.fromPage(responsePage));
    }

    /**
     * Получить баланс конкретной карты
     */
    @GetMapping("/{cardNumber}/balance")
    @Operation(
        summary = "Получить баланс карты",
        description = "Получение баланса конкретной карты пользователя"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Баланс получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BalanceResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль USER"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Карта не найдена"
        )
    })
    public ResponseEntity<BalanceResponse> getCardBalance(
            @Parameter(description = "Номер карты", required = true)
            @PathVariable String cardNumber) {
        
        String username = getCurrentUsername();
        List<Card> userCards = cardService.getCardsByUser(username);
        
        Card card = userCards.stream()
            .filter(c -> c.getCardNumber().equals(cardNumber))
            .findFirst()
            .orElse(null);
        
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        
        BalanceResponse response = new BalanceResponse();
        response.setCardNumber(CardMaskingUtil.maskCardNumber(card.getCardNumber()));
        response.setBalance(card.getBalance());
        response.setCurrency("RUB");
        response.setStatus(card.getStatus().getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Запросить блокировку карты
     */
    @PostMapping("/{cardNumber}/block-request")
    @Operation(
        summary = "Запросить блокировку карты",
        description = "Отправка запроса на блокировку карты (требует подтверждения администратора)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Запрос на блокировку отправлен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BlockRequestResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Карта уже заблокирована, неактивна или запрос уже отправлен"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль USER"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Карта не найдена"
        )
    })
    public ResponseEntity<BlockRequestResponse> requestCardBlock(
            @Parameter(description = "Номер карты", required = true)
            @PathVariable String cardNumber,
            @Parameter(description = "Причина блокировки", required = true)
            @RequestParam String reason) {
        
        String username = getCurrentUsername();
        List<Card> userCards = cardService.getCardsByUser(username);
        
        Card card = userCards.stream()
            .filter(c -> c.getCardNumber().equals(cardNumber))
            .findFirst()
            .orElse(null);
        
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            cardService.createBlockRequest(cardNumber, reason);
            
            BlockRequestResponse response = new BlockRequestResponse(true, 
                "Запрос на блокировку карты " + CardMaskingUtil.maskCardNumber(card.getCardNumber()) + " отправлен администратору. Причина: " + reason);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new BlockRequestResponse(false, e.getMessage())
            );
        }
    }

    /**
     * Перевод между своими картами
     */
    @PostMapping("/transfer")
    @Operation(
        summary = "Перевод между своими картами",
        description = "Выполнение перевода денег между картами текущего пользователя"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Перевод выполнен успешно",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransferResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации или недостаточно средств"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль USER"
        )
    })
    public ResponseEntity<TransferResponseDTO> transferBetweenMyCards(
            @Parameter(description = "Данные для перевода", required = true)
            @RequestBody TransferRequestDTO request) {
        
        String username = getCurrentUsername();
        List<Card> userCards = cardService.getCardsByUser(username);
        
        boolean cardFromBelongsToUser = userCards.stream()
            .anyMatch(card -> card.getCardNumber().equals(request.getCardFromNumber()));
        
        boolean cardToBelongsToUser = userCards.stream()
            .anyMatch(card -> card.getCardNumber().equals(request.getCardToNumber()));
        
        if (!cardFromBelongsToUser || !cardToBelongsToUser) {
            return ResponseEntity.badRequest().body(
                TransferResponseDTO.failure(
                    request.getCardFromNumber(),
                    request.getCardToNumber(),
                    request.getAmount(),
                    request.getDescription(),
                    "Обе карты должны принадлежать текущему пользователю"
                )
            );
        }
        
        TransferResponseDTO response = transactionService.transferMoney(request);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public static class BalanceResponse {
        private String cardNumber;
        private BigDecimal balance;
        private String currency;
        private String status;

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BlockRequestResponse {
        private boolean success;
        private String message;

        public BlockRequestResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Геттеры и сеттеры
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 