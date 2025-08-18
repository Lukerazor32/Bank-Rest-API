package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "card_status")
public class CardStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_blocked")
    private boolean isBlocked = false;

    @Column(name = "is_expired")
    private boolean isExpired = false;

    @JsonBackReference
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cards;

    public CardStatus() {}

    public CardStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CardStatus(String name, String description, boolean isActive, boolean isBlocked, boolean isExpired) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.isBlocked = isBlocked;
        this.isExpired = isExpired;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public boolean isCardUsable() {
        return isActive && !isBlocked && !isExpired;
    }

    public boolean isCardBlocked() {
        return isBlocked;
    }

    public boolean isCardExpired() {
        return isExpired;
    }

    public boolean isCardActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardStatus that = (CardStatus) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
} 