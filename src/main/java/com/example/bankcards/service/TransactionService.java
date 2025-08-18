package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.TransactionType;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardStatusService cardStatusService;

    /**
     * Выполнить перевод денег между картами
     */
    @Transactional
    public TransferResponseDTO transferMoney(TransferRequestDTO request) {
        try {
            // Валидация входных данных
            validateTransferRequest(request);
            
            // Получаем карты
            Card cardFrom = getCardByNumber(request.getCardFromNumber());
            Card cardTo = getCardByNumber(request.getCardToNumber());
            
            // Проверяем возможность перевода
            validateTransfer(cardFrom, cardTo, request.getAmount());
            
            // Проверяем достаточность средств
            if (cardFrom.getBalance().compareTo(request.getAmount()) < 0) {
                return TransferResponseDTO.failure(
                    request.getCardFromNumber(),
                    request.getCardToNumber(),
                    request.getAmount(),
                    request.getDescription(),
                    "Недостаточно средств. Требуется: " + request.getAmount() + " руб."
                );
            }
            
            // Создаем транзакцию
            Transaction transaction = new Transaction(cardFrom, cardTo, request.getAmount(), TransactionType.TRANSFER, request.getDescription());
            transaction.setTotalAmount(request.getAmount());
            transaction.setStatus(TransactionStatus.PENDING);
            
            // Выполняем перевод
            performTransfer(cardFrom, cardTo, request.getAmount());
            
            // Обновляем статус транзакции
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedDate(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            return TransferResponseDTO.success(
                savedTransaction.getId(),
                request.getCardFromNumber(),
                request.getCardToNumber(),
                request.getAmount(),
                request.getDescription(),
                savedTransaction.getTransactionDate()
            );
            
        } catch (Exception e) {
            return TransferResponseDTO.failure(
                request.getCardFromNumber(),
                request.getCardToNumber(),
                request.getAmount(),
                request.getDescription(),
                "Ошибка перевода: " + e.getMessage()
            );
        }
    }

    /**
     * Получить все транзакции по карте
     */
    public List<Transaction> getTransactionsByCard(String cardNumber) {
        return transactionRepository.findByCardNumber(cardNumber);
    }

    /**
     * Получить транзакции по пользователю
     */
    public List<Transaction> getTransactionsByUser(String username) {
        return transactionRepository.findByUsername(username);
    }

    /**
     * Получить транзакции по статусу
     */
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    /**
     * Получить транзакции по диапазону дат
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    /**
     * Получить все транзакции в системе
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Получить статистику по транзакциям
     */
    public TransactionStatistics getTransactionStatistics(String cardNumber) {
        long totalTransactions = transactionRepository.countByCardNumber(cardNumber);
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long failedTransactions = transactionRepository.countByStatus(TransactionStatus.FAILED);
        
        return new TransactionStatistics(totalTransactions, completedTransactions, failedTransactions);
    }

    // Приватные методы

    private void validateTransferRequest(TransferRequestDTO request) {
        if (request.getCardFromNumber().equals(request.getCardToNumber())) {
            throw new RuntimeException("Нельзя переводить деньги на ту же карту");
        }
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма перевода должна быть больше 0");
        }
    }

    private Card getCardByNumber(String cardNumber) {
        Optional<Card> cardOpt = cardRepository.findByCardNumber(cardNumber);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта с номером " + cardNumber + " не найдена");
        }
        return cardOpt.get();
    }

    private void validateTransfer(Card cardFrom, Card cardTo, BigDecimal amount) {
        // Проверяем статус карты отправителя
        if (!cardStatusService.isCardUsable(cardFrom.getStatus())) {
            throw new RuntimeException("Карта отправителя не активна");
        }
        
        // Проверяем статус карты получателя
        if (!cardStatusService.isCardUsable(cardTo.getStatus())) {
            throw new RuntimeException("Карта получателя не активна");
        }
        
        // Проверяем, не истекла ли карта отправителя
        if (cardFrom.isExpired()) {
            throw new RuntimeException("Карта отправителя истекла");
        }
        
        // Проверяем, не истекла ли карта получателя
        if (cardTo.isExpired()) {
            throw new RuntimeException("Карта получателя истекла");
        }
    }

    private void performTransfer(Card cardFrom, Card cardTo, BigDecimal amount) {
        // Списываем деньги с карты отправителя
        cardFrom.setBalance(cardFrom.getBalance().subtract(amount));
        cardRepository.save(cardFrom);
        
        // Зачисляем деньги на карту получателя
        cardTo.setBalance(cardTo.getBalance().add(amount));
        cardRepository.save(cardTo);
    }

    /**
     * Внутренний класс для статистики транзакций
     */
    public static class TransactionStatistics {
        private final long totalTransactions;
        private final long completedTransactions;
        private final long failedTransactions;

        public TransactionStatistics(long totalTransactions, long completedTransactions, long failedTransactions) {
            this.totalTransactions = totalTransactions;
            this.completedTransactions = completedTransactions;
            this.failedTransactions = failedTransactions;
        }

        // Геттеры
        public long getTotalTransactions() { return totalTransactions; }
        public long getCompletedTransactions() { return completedTransactions; }
        public long getFailedTransactions() { return failedTransactions; }
        public long getPendingTransactions() { return totalTransactions - completedTransactions - failedTransactions; }
        public double getSuccessRate() { 
            return totalTransactions > 0 ? (double) completedTransactions / totalTransactions * 100 : 0; 
        }
    }
} 