package com.alhajri.goldPrice.util;

import com.alhajri.goldPrice.DTO.TeleChatDTO;
import com.alhajri.goldPrice.services.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class TelegramUtility {
    private static final Logger log = LoggerFactory.getLogger(TelegramUtility.class);


    // ========== SIMPLE TEXT MESSAGE ==========
    public static void sendText(TelegramBot bot, Long chatID, String text) {
        send(bot, TeleChatDTO.create()
                .text(text)
                .build(chatID)
        );
    }

    // ========== SAFE SEND WRAPPER ==========
    private static void send(TelegramBot bot, SendMessage msg) {
        try {
            bot.execute(msg);
        } catch (Exception e) {
            log.error("Telegram API Error", e);
        }
    }
}
