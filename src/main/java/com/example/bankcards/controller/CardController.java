package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CardStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.dto.PaginatedResponse;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
@Tag(name = "Управление картами", description = "API для управления банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardStatusService cardStatusService;



    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Поиск карт с фильтрами и пагинацией",
        description = "Универсальный поиск карт с возможностью фильтрации по различным критериям и пагинацией"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список карт получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaginatedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> searchCards(
            @Parameter(description = "Имя пользователя для фильтрации")
            @RequestParam(required = false) String username,
            @Parameter(description = "Статус карты для фильтрации (ACTIVE, BLOCKED, NON_ACTIVE)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Поиск по номеру карты")
            @RequestParam(required = false) String search,
            @Parameter(description = "Фильтр по запросам на блокировку")
            @RequestParam(defaultValue = "false") boolean hasBlockRequest,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки (id, cardNumber, balance, expirationDate, createdAt)")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Card> cardsPage = cardService.getCardsWithFilters(
            username, status, search, hasBlockRequest, page, size, sortBy, sortDir
        );
        
        Page<CardResponseDTO> responsePage = cardsPage.map(CardResponseDTO::fromCard);
        return ResponseEntity.ok(PaginatedResponse.fromPage(responsePage));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Статистика карт",
        description = "Получение статистики по картам (общее количество, активные, заблокированные и т.д.)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Статистика получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardService.CardStatistics.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardService.CardStatistics> getCardStatistics() {
        CardService.CardStatistics stats = cardService.getCardStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Создать карту",
        description = "Создание новой банковской карты для пользователя. " +
                     "ВАЖНО: Нельзя создавать карты для администраторов по соображениям безопасности."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно создана",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при создании карты (включая попытку создать карту для администратора)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> createCard(
            @Parameter(description = "Данные для создания карты", required = true)
            @RequestBody CreateCardRequest request) {
        try {
            Card newCard = cardService.createCard(
                request.getCardNumber(),
                request.getUsername(),
                request.getExpirationDate(),
                request.getInitialBalance()
            );
            return ResponseEntity.ok(CardResponseDTO.fromCard(newCard));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{cardNumber}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Активировать карту",
        description = "Активация заблокированной или неактивной карты"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно активирована",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при активации карты"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> activateCard(
            @Parameter(description = "номер карты", required = true)
            @PathVariable String cardNumber) {
        try {
            Card card = cardService.activateCard(cardNumber);
            return ResponseEntity.ok(CardResponseDTO.fromCard(card));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{cardNumber}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Заблокировать карту",
        description = "Блокировка активной карты"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно заблокирована",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при блокировке карты"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> blockCard(
            @Parameter(description = "номер карты", required = true)
            @PathVariable String cardNumber) {
        try {
            Card card = cardService.blockCard(cardNumber);
            return ResponseEntity.ok(CardResponseDTO.fromCard(card));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{cardNumber}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Деактивировать карту",
        description = "Деактивация активной карты"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно деактивирована",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при деактивации карты"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> deactivateCard(
            @Parameter(description = "номер карты", required = true)
            @PathVariable String cardNumber) {
        try {
            Card card = cardService.deactivateCard(cardNumber);
            return ResponseEntity.ok(CardResponseDTO.fromCard(card));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{cardNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Удалить карту",
        description = "Удаление карты из системы"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно удалена"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при удалении карты"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "номер карты", required = true)
            @PathVariable String cardNumber) {
        try {
            cardService.deleteCard(cardNumber);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{cardNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Обновить карту",
        description = "Обновление данных карты. " +
                     "ВАЖНО: Нельзя назначать карты администраторам по соображениям безопасности."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Карта успешно обновлена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка при обновлении карты (включая попытку назначить карту администратору)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> updateCard(
            @Parameter(description = "Номер карты", required = true)
            @PathVariable String cardNumber,
            @Parameter(description = "Данные для обновления карты", required = true)
            @RequestBody UpdateCardRequest request) {
        try {
            Card card = cardService.updateCard(
                cardNumber,
                request.getUsername(),
                request.getExpirationDate(),
                request.getBalance()
            );
            return ResponseEntity.ok(CardResponseDTO.fromCard(card));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }



    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить карты пользователя",
        description = "Получение списка всех карт конкретного пользователя с пагинацией"
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
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getCardsByUser(
            @Parameter(description = "Имя пользователя", required = true)
            @PathVariable String username,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки (id, cardNumber, balance, expirationDate, createdAt)")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Card> cardsPage = cardService.getCardsWithFilters(
            username, null, null, false, page, size, sortBy, sortDir
        );
        
        Page<CardResponseDTO> responsePage = cardsPage.map(CardResponseDTO::fromCard);
        return ResponseEntity.ok(PaginatedResponse.fromPage(responsePage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить карту по ID",
        description = "Получение информации о конкретной карте по её ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Информация о карте получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Карта не найдена"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> getCardById(
            @Parameter(description = "ID карты", required = true)
            @PathVariable Long id) {
        return cardService.getCardById(id)
                .map(card -> ResponseEntity.ok(CardResponseDTO.fromCard(card)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить карту по номеру",
        description = "Получение информации о конкретной карте по её номеру"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Информация о карте получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Карта не найдена"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<CardResponseDTO> getCardByNumber(
            @Parameter(description = "Номер карты", required = true)
            @PathVariable String cardNumber) {
        return cardService.getCardByNumber(cardNumber)
                .map(card -> ResponseEntity.ok(CardResponseDTO.fromCard(card)))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/block-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Получить все запросы на блокировку карт",
        description = "Получение списка всех карт с запросами на блокировку для рассмотрения администратором с пагинацией"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список запросов на блокировку получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaginatedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен - требуется роль ADMIN"
        )
    })
    public ResponseEntity<PaginatedResponse<BlockRequestDTO>> getBlockRequests(
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки (id, cardNumber, balance, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Card> cardsWithRequests = cardService.getCardsWithFilters(
            null, null, null, true, page, size, sortBy, sortDir
        );
        
        Page<BlockRequestDTO> responsePage = cardsWithRequests.map(BlockRequestDTO::fromCard);
        return ResponseEntity.ok(PaginatedResponse.fromPage(responsePage));
    }



    public static class CreateCardRequest {
        private String cardNumber;
        private String username;
        private LocalDate expirationDate;
        private BigDecimal initialBalance;

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    }

    public static class UpdateCardRequest {
        private String cardNumber;
        private String username;
        private LocalDate expirationDate;
        private BigDecimal balance;

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
    }
} 