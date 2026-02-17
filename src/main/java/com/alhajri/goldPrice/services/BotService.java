package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.entity.TelegramUser;
import com.alhajri.goldPrice.repository.TelegramUserRepository;
import com.alhajri.goldPrice.util.TelegramUtility;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final TelegramUserRepository repository;

    // In-memory cache for quick broadcasts (keeps parity with DB)
    private final Set<Long> activeChatIds = new HashSet<>();


    public BotService(TelegramUserRepository repository) {
        this.repository = repository;
    }
    @PostConstruct
    private void initActiveChatIds() {
        // load persisted active users safely after Spring context is ready
        repository.findAll().forEach(u -> activeChatIds.add(u.getChatId()));
    }
    public void registerBotCommands(TelegramBot bot, Long chatID) {
        try {
            // Track this chat for price notifications
            addActiveChatId(chatID);

            List<BotCommand> commands = List.of(
                    new BotCommand("/start", "Start the bot"),
                    new BotCommand("/price", "the price now")
            );

            bot.execute(SetMyCommands.builder()
                    .commands(commands)
                    .scope(new BotCommandScopeChat(chatID.toString())) // per-user scope
                    .build());

        } catch (Exception e) {
            logger.error("Failed to register bot commands", e);
        }
    }

    /**
     * Add a chat ID to receive price notifications and persist it
     */
    public void addActiveChatId(Long chatId) {
        if (activeChatIds.add(chatId)) {
            TelegramUser u = repository.findById(chatId).orElseGet(() -> {
                TelegramUser user = new TelegramUser();
                user.setChatId(chatId);
                user.setLastSentCfd(0L);
                return user;
            });
            repository.save(u);
            logger.info("✅ USER REGISTERED: Chat ID {} added to broadcast list (Total active: {})",
                    chatId, activeChatIds.size());
        }
    }


    /**
     * Remove a chat ID from price notifications and DB
     */
    public void removeChatId(Long chatId) {
        if (activeChatIds.remove(chatId)) {
            repository.deleteById(chatId);
            logger.info("❌ USER UNREGISTERED: Chat ID {} removed from broadcast list (Total active: {})",
                    chatId, activeChatIds.size());
        }
    }

    /**
     * Get all active chat IDs
     */
    public Set<Long> getActiveChatIds() {
        return new HashSet<>(activeChatIds);
    }

    /**
     * Broadcast a message to all connected Telegram users
     */
    public void broadcastToAllChats(TelegramBot bot, String message) {
        Set<Long> chatIds = getActiveChatIds();
        logger.info("📢 Broadcasting to {} user(s)...", chatIds.size());

        int successCount = 0;
        int failureCount = 0;

        for (Long chatId : chatIds) {
            try {
                TelegramUtility.sendText(bot, chatId, message);
                successCount++;
                logger.debug("  ✓ Sent to chat {}", chatId);
            } catch (Exception e) {
                failureCount++;
                logger.error("  ✗ Failed to send to chat {}: {}", chatId, e.getMessage());
                // Remove inactive chat IDs
                removeChatId(chatId);
            }
        }

        logger.info("📊 Broadcast complete - Success: {}, Failed: {}", successCount, failureCount);
    }

    public void updateLastSentCfd(Long chatId, Long cfd) {
        repository.findById(chatId).ifPresent(u -> {
            u.setLastSentCfd(cfd);
            repository.save(u);
        });
    }

}
