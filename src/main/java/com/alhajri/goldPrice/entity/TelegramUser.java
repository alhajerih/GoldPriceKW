package com.alhajri.goldPrice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "telegram_users")
public class TelegramUser {
    @Id
    private Long chatId;
    private Long lastSentCfd;
}

