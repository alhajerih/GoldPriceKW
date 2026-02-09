package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.util.TelegramUtility;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final String botToken;
    private final BotService service;

    public TelegramBot(String botName, String botToken, BotService service) {
        this.botName = botName;
        this.botToken = botToken;
        this.service = service;
    }

    private void handleUpdate(Update update) {
        Long chatId;
        Long userId;
        String text = null;
        Message message = null;

        // Normal message
        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = message.getChatId();
            userId = message.getFrom().getId();
            text = message.hasText() ? message.getText() : null;
        }
        // Callback query
        else if (update.hasCallbackQuery()) {
            var cb = update.getCallbackQuery();
            chatId = cb.getMessage() != null
                    ? cb.getMessage().getChatId()
                    : cb.getFrom().getId();
            userId = cb.getFrom().getId();
            text = cb.getData();
        } else {
            return;
        }

        // Track this chat for price notifications
        service.addActiveChatId(chatId);

        service.registerBotCommands(this, chatId);

        TelegramUtility.sendText(this, chatId, "مرحباً! ستتلقى الآن تحديثات مباشره لأسعار الذهب.");

    }



    @Override
    public void onUpdateReceived(Update update) {
        handleUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
