package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardStatusService {

    @Autowired
    private CardStatusRepository cardStatusRepository;

    /**
     * Получить все статусы карт
     */
    public List<CardStatus> getAllCardStatuses() {
        return cardStatusRepository.findAll();
    }

    /**
     * Получить статус по ID
     */
    public Optional<CardStatus> getCardStatusById(Long id) {
        return cardStatusRepository.findById(id);
    }

    /**
     * Получить статус по имени
     */
    public Optional<CardStatus> getCardStatusByName(String name) {
        return cardStatusRepository.findByName(name);
    }

    /**
     * Получить активные статусы
     */
    public List<CardStatus> getActiveCardStatuses() {
        return cardStatusRepository.findByIsActive(true);
    }

    /**
     * Получить заблокированные статусы
     */
    public List<CardStatus> getBlockedCardStatuses() {
        return cardStatusRepository.findByIsBlocked(true);
    }

    /**
     * Получить истекшие статусы
     */
    public List<CardStatus> getExpiredCardStatuses() {
        return cardStatusRepository.findByIsExpired(true);
    }

    /**
     * Получить пригодные для использования статусы
     */
    public List<CardStatus> getUsableCardStatuses() {
        return cardStatusRepository.findUsableStatuses();
    }

    /**
     * Создать новый статус
     */
    public CardStatus createCardStatus(String name, String description, boolean isActive, boolean isBlocked, boolean isExpired) {
        if (cardStatusRepository.existsByName(name)) {
            throw new RuntimeException("Статус с именем '" + name + "' уже существует");
        }

        CardStatus cardStatus = new CardStatus(name, description, isActive, isBlocked, isExpired);
        return cardStatusRepository.save(cardStatus);
    }

    /**
     * Обновить статус
     */
    public CardStatus updateCardStatus(Long id, String name, String description, boolean isActive, boolean isBlocked, boolean isExpired) {
        CardStatus cardStatus = cardStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Статус с ID " + id + " не найден"));

        cardStatus.setName(name);
        cardStatus.setDescription(description);
        cardStatus.setActive(isActive);
        cardStatus.setBlocked(isBlocked);
        cardStatus.setExpired(isExpired);

        return cardStatusRepository.save(cardStatus);
    }

    /**
     * Удалить статус
     */
    public void deleteCardStatus(Long id) {
        CardStatus cardStatus = cardStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Статус с ID " + id + " не найден"));

        // Проверяем, используется ли статус
        if (cardStatus.getCards() != null && !cardStatus.getCards().isEmpty()) {
            throw new RuntimeException("Нельзя удалить статус, который используется картами");
        }

        cardStatusRepository.delete(cardStatus);
    }

    /**
     * Получить статус "Активная"
     */
    public CardStatus getActiveStatus() {
        return cardStatusRepository.findByName("ACTIVE")
                .orElseThrow(() -> new RuntimeException("Статус 'ACTIVE' не найден"));
    }

    /**
     * Получить статус "Заблокированная"
     */
    public CardStatus getBlockedStatus() {
        return cardStatusRepository.findByName("BLOCKED")
                .orElseThrow(() -> new RuntimeException("Статус 'BLOCKED' не найден"));
    }

    /**
     * Получить статус "Неактивная"
     */
    public CardStatus getNonActiveStatus() {
        return cardStatusRepository.findByName("NONACTIVE")
                .orElseThrow(() -> new RuntimeException("Статус 'NONACTIVE' не найден"));
    }

    /**
     * Получить статус "Истекшая"
     */
    public CardStatus getExpiredStatus() {
        return cardStatusRepository.findByName("EXPIRED")
                .orElseThrow(() -> new RuntimeException("Статус 'EXPIRED' не найден"));
    }

    /**
     * Проверить, можно ли использовать карту с данным статусом
     */
    public boolean isCardUsable(CardStatus status) {
        return status != null && status.isCardUsable();
    }

    /**
     * Проверить, заблокирована ли карта
     */
    public boolean isCardBlocked(CardStatus status) {
        return status != null && status.isCardBlocked();
    }

    /**
     * Проверить, истекла ли карта
     */
    public boolean isCardExpired(CardStatus status) {
        return status != null && status.isCardExpired();
    }
} 