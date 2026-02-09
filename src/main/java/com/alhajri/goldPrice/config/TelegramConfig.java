package com.alhajri.goldPrice.config;

import com.alhajri.goldPrice.services.BotService;
import com.alhajri.goldPrice.services.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramConfig {
    private static final Logger logger = LoggerFactory.getLogger(TelegramConfig.class);

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties, BotService service) throws TelegramApiException {
        logger.info("Initializing TelegramBot with properties - Name: '{}', Token: '{}'",
            properties.getBotName(),
            properties.getBotToken() != null ? "***" : "NULL");

        // Validate properties
        if (properties.getBotName() == null || properties.getBotName().trim().isEmpty()) {
            throw new IllegalArgumentException("telegram.bot.bot-name property is required and cannot be empty");
        }
        if (properties.getBotToken() == null || properties.getBotToken().trim().isEmpty()) {
            throw new IllegalArgumentException("telegram.bot.bot-token property is required and cannot be empty");
        }

        TelegramBot bot = new TelegramBot(properties.getBotName(), properties.getBotToken(), service);
        logger.info("TelegramBot instance created successfully");

        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        logger.info("TelegramBot registered with TelegramBotsApi");

        return bot;
    }
}
