package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Optional<Card> findByCardNumber(String cardNumber);
    
    // Получить карты по имени пользователя
    @Query("SELECT c FROM Card c JOIN c.user u WHERE u.username = :username")
    List<Card> findByUsername(@Param("username") String username);
    
    // Получить карты по статусу
    List<Card> findByStatus(CardStatus status);
    
    // Получить карты с истекшим сроком действия
    List<Card> findByExpirationDateBefore(LocalDate date);
    
    // Получить карты с балансом больше указанной суммы
    List<Card> findByBalanceGreaterThan(BigDecimal amount);
    
    // Получить карты с балансом меньше указанной суммы
    List<Card> findByBalanceLessThan(BigDecimal amount);
    
    // Проверить существование карты по номеру
    boolean existsByCardNumber(String cardNumber);
    
    // Подсчитать количество карт по статусу
    long countByStatus(CardStatus status);
    
    // Подсчитать количество карт с балансом больше указанной суммы
    @Query("SELECT COUNT(c) FROM Card c WHERE c.balance > :amount")
    long countByBalanceGreaterThan(@Param("amount") BigDecimal amount);
    
    // Обновить статус карты
    @Modifying
    @Query("UPDATE Card c SET c.status = :status WHERE c.id = :cardId")
    void updateCardStatus(@Param("cardId") Long cardId, @Param("status") CardStatus status);
    
    // Обновить баланс карты
    @Modifying
    @Query("UPDATE Card c SET c.balance = :balance WHERE c.id = :cardId")
    void updateCardBalance(@Param("cardId") Long cardId, @Param("balance") BigDecimal balance);
    
    // Получить карты с запросами на блокировку
    List<Card> findByHasBlockRequestTrue();
} 