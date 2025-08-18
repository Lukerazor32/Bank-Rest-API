package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Найти транзакции по карте отправителя
    @Query("SELECT t FROM Transaction t WHERE t.cardFrom.cardNumber = :cardNumber")
    List<Transaction> findByCardFromNumber(@Param("cardNumber") String cardNumber);
    
    // Найти транзакции по карте получателя
    @Query("SELECT t FROM Transaction t WHERE t.cardTo.cardNumber = :cardNumber")
    List<Transaction> findByCardToNumber(@Param("cardNumber") String cardNumber);
    
    // Найти все транзакции по карте (входящие и исходящие)
    @Query("SELECT t FROM Transaction t WHERE t.cardFrom.cardNumber = :cardNumber OR t.cardTo.cardNumber = :cardNumber")
    List<Transaction> findByCardNumber(@Param("cardNumber") String cardNumber);
    
    // Найти транзакции по статусу
    List<Transaction> findByStatus(TransactionStatus status);
    
    // Найти транзакции по диапазону дат
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Найти транзакции по пользователю (по его картам)
    @Query("SELECT t FROM Transaction t WHERE t.cardFrom.user.username = :username OR t.cardTo.user.username = :username")
    List<Transaction> findByUsername(@Param("username") String username);
    
    // Найти транзакции по статусу и карте
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND (t.cardFrom.cardNumber = :cardNumber OR t.cardTo.cardNumber = :cardNumber)")
    List<Transaction> findByStatusAndCardNumber(@Param("status") TransactionStatus status, @Param("cardNumber") String cardNumber);
    
    // Найти транзакции по сумме (больше указанной)
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount")
    List<Transaction> findByAmountGreaterThan(@Param("amount") java.math.BigDecimal amount);
    
    // Найти транзакции по сумме (меньше указанной)
    @Query("SELECT t FROM Transaction t WHERE t.amount < :amount")
    List<Transaction> findByAmountLessThan(@Param("amount") java.math.BigDecimal amount);
    
    // Подсчитать количество транзакций по статусу
    long countByStatus(TransactionStatus status);
    
    // Подсчитать количество транзакций по карте
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.cardFrom.cardNumber = :cardNumber OR t.cardTo.cardNumber = :cardNumber")
    long countByCardNumber(@Param("cardNumber") String cardNumber);
} 