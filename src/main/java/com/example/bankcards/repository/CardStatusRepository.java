package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardStatusRepository extends JpaRepository<CardStatus, Long> {

    Optional<CardStatus> findByName(String name);

    List<CardStatus> findByIsActive(boolean isActive);

    List<CardStatus> findByIsBlocked(boolean isBlocked);

    List<CardStatus> findByIsExpired(boolean isExpired);

    @Query("SELECT cs FROM CardStatus cs WHERE cs.isActive = true AND cs.isBlocked = false AND cs.isExpired = false")
    List<CardStatus> findUsableStatuses();

    @Query("SELECT cs FROM CardStatus cs WHERE cs.isBlocked = true")
    List<CardStatus> findBlockedStatuses();

    @Query("SELECT cs FROM CardStatus cs WHERE cs.isExpired = true")
    List<CardStatus> findExpiredStatuses();

    boolean existsByName(String name);
} 