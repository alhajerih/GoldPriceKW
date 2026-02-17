package com.alhajri.goldPrice.repository;

import com.alhajri.goldPrice.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
}

