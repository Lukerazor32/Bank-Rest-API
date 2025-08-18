package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private String exceptionMessage = "Карта с номером %s не найдена";

    @Autowired
    private CardStatusService cardStatusService;

    /**
     * Получить карты пользователя
     */
    public List<Card> getCardsByUser(String username) {
        return cardRepository.findByUsername(username);
    }

    /**
     * Получить карту по ID
     */
    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    /**
     * Получить карту по номеру
     */
    public Optional<Card> getCardByNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber);
    }

    /**
     * Универсальный метод для получения карт с фильтрами и пагинацией
     */
    public Page<Card> getCardsWithFilters(
            String username,
            String status,
            String search,
            boolean hasBlockRequest,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(
            sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
            sortBy
        ));
        
        Specification<Card> spec = Specification.where(null);
        
        if (username != null && !username.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("user").get("username"), username));
        }
        
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("status").get("name"), status));
        }
        
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("cardNumber")), "%" + search.toLowerCase() + "%"));
        }
        
        if (hasBlockRequest) {
            spec = spec.and((root, query, cb) -> 
                cb.isTrue(root.get("hasBlockRequest")));
        }
        
        return cardRepository.findAll(spec, pageable);
    }

    /**
     * Создать новую карту
     */
    @Transactional
    public Card createCard(String cardNumber, String username, LocalDate expirationDate, BigDecimal balance) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("Пользователь с именем " + username + " не найден");
        }

        if (user.get().getRole().isAdmin()) {
            throw new RuntimeException("Нельзя создавать карты для администраторов по соображениям безопасности");
        }

        if (cardRepository.existsByCardNumber(cardNumber)) {
            throw new RuntimeException("Карта с номером " + cardNumber + " уже существует");
        }

        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setUser(user.get());
        card.setExpirationDate(expirationDate);
        card.setStatus(cardStatusService.getNonActiveStatus());
        card.setBalance(balance);

        return cardRepository.save(card);
    }

    /**
     * Обновить карту
     */
    @Transactional
    public Card updateCard(String cardNumber, String username, LocalDate expirationDate, BigDecimal balance) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                throw new RuntimeException("Пользователь с именем " + username + " не найден");
            }
            
            if (user.get().getRole().isAdmin()) {
                throw new RuntimeException("Нельзя назначать карты администраторам по соображениям безопасности");
            }
            
            card.setUser(user.get());
        }

        if (cardNumber != null) {
            if (!cardNumber.equals(card.getCardNumber()) && cardRepository.existsByCardNumber(cardNumber)) {
                throw new RuntimeException("Карта с номером " + cardNumber + " уже существует");
            }
            card.setCardNumber(cardNumber);
        }

        if (expirationDate != null) {
            card.setExpirationDate(expirationDate);
        }

        if (balance != null) {
            card.setBalance(balance);
        }

        return cardRepository.save(card);
    }

    /**
     * Активировать карту
     */
    @Transactional
    public Card activateCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        card.setStatus(cardStatusService.getActiveStatus());
        return cardRepository.save(card);
    }

    /**
     * Заблокировать карту
     */
    @Transactional
    public Card blockCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        card.setStatus(cardStatusService.getBlockedStatus());
        return cardRepository.save(card);
    }

    /**
     * Деактивировать карту
     */
    @Transactional
    public Card deactivateCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        card.setStatus(cardStatusService.getNonActiveStatus());
        return cardRepository.save(card);
    }

    /**
     * Удалить карту
     */
    @Transactional
    public void deleteCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        cardRepository.delete(card);
    }

    /**
     * Получить статистику карт
     */
    public CardStatistics getCardStatistics() {
        long totalCards = cardRepository.count();
        long activeCards = cardRepository.countByStatus(cardStatusService.getActiveStatus());
        long blockedCards = cardRepository.countByStatus(cardStatusService.getBlockedStatus());
        long nonActiveCards = cardRepository.countByStatus(cardStatusService.getNonActiveStatus());
        long expiredCards = cardRepository.countByStatus(cardStatusService.getExpiredStatus());

        return new CardStatistics(totalCards, activeCards, blockedCards, nonActiveCards, expiredCards);
    }

    /**
     * Внутренний класс для статистики карт
     */
    public static class CardStatistics {
        private final long totalCards;
        private final long activeCards;
        private final long blockedCards;
        private final long nonActiveCards;
        private final long expiredCards;

        public CardStatistics(long totalCards, long activeCards, long blockedCards, long nonActiveCards, long expiredCards) {
            this.totalCards = totalCards;
            this.activeCards = activeCards;
            this.blockedCards = blockedCards;
            this.nonActiveCards = nonActiveCards;
            this.expiredCards = expiredCards;
        }

        public long getTotalCards() { return totalCards; }
        public long getActiveCards() { return activeCards; }
        public long getBlockedCards() { return blockedCards; }
        public long getNonActiveCards() { return nonActiveCards; }
        public long getExpiredCards() { return expiredCards; }
        public double getActivePercentage() { 
            return totalCards > 0 ? (double) activeCards / totalCards * 100 : 0; 
        }
    }

    /**
     * Создать запрос на блокировку карты
     */
    @Transactional
    public Card createBlockRequest(String cardNumber, String reason) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        if (!card.getStatus().getName().equals("ACTIVE")) {
            throw new RuntimeException("Карта уже заблокирована или неактивна");
        }

        if (card.isHasBlockRequest()) {
            throw new RuntimeException("Запрос на блокировку карты уже существует");
        }

        card.createBlockRequest(reason);
        return cardRepository.save(card);
    }

    /**
     * Получить все карты с запросами на блокировку
     */
    public List<Card> getCardsWithBlockRequests() {
        return cardRepository.findByHasBlockRequestTrue();
    }

    /**
     * Подтвердить блокировку карты (админ)
     */
    @Transactional
    public Card confirmBlockRequest(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        if (!card.isHasBlockRequest()) {
            throw new RuntimeException("Запрос на блокировку карты не найден");
        }

        card.setStatus(cardStatusService.getBlockedStatus());
        card.cancelBlockRequest();
        
        return cardRepository.save(card);
    }

    /**
     * Отклонить запрос на блокировку карты (админ)
     */
    @Transactional
    public Card rejectBlockRequest(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));

        if (!card.isHasBlockRequest()) {
            throw new RuntimeException("Запрос на блокировку карты не найден");
        }

        card.cancelBlockRequest();
        
        return cardRepository.save(card);
    }

    /**
     * Проверить, есть ли у карты запрос на блокировку
     */
    public boolean hasBlockRequest(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException(String.format(exceptionMessage, cardNumber)));
        
        return card.isHasBlockRequest();
    }
} 