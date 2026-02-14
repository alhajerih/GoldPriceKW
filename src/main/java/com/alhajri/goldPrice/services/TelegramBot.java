package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.util.GoldCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Telegram bot that understands Arabic "جرام/غرام" sell requests and returns KD price.
 */
public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final String botName;
    private final String botToken;
    private final BotService service;
    private final LiveMetalPriceService priceService;

    // Regex: western (0-9) or Arabic-Indic (٠-٩) digits, optional decimal part (comma or dot)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9\u0660-\u0669]+(?:[.,][0-9\u0660-\u0669]+)?)");

    public TelegramBot(String botName, String botToken, BotService service, LiveMetalPriceService priceService) {
        this.botName = botName;
        this.botToken = botToken;
        this.service = service;
        this.priceService = priceService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message msg = update.getMessage();
                Long chatId = msg.getChatId();
                String text = msg.hasText() ? msg.getText() : "";
                logger.debug("Incoming message from {}: {}", chatId, text);
                handleText(chatId, text);
            } else if (update.hasCallbackQuery()) {
                var cb = update.getCallbackQuery();
                String data = cb.getData() != null ? cb.getData() : "";
                Long chatId = cb.getMessage() != null ? cb.getMessage().getChatId() : cb.getFrom().getId();
                logger.debug("Incoming callback from {}: {}", chatId, data);
                handleText(chatId, data);
            }
        } catch (Exception e) {
            logger.error("Error handling update", e);
        }
    }

    private void handleText(Long chatId, String text) {
        if (text == null) text = "";
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return;

        // If user sends /start — register and send welcome once
        if (trimmed.startsWith("/start")) {
            service.addActiveChatId(chatId);
            service.registerBotCommands(this, chatId);
            sendText(chatId, "تم التسجيل !!");
            sendText(chatId, "مرحباً! ستتلقى الآن تحديثات مباشرًة لأسعار الذهب.\n  لمعرفه سعر البيع أرسل مثلاً: 5 جرام");
            return;
        }
        if (trimmed.startsWith("/price")) {
            service.addActiveChatId(chatId);
            service.registerBotCommands(this, chatId);
            sendText(chatId,priceService.getLatestGoldPrices(chatId));
            return;
        }
        // register chat and store active status (idempotent)
        service.addActiveChatId(chatId);
        service.registerBotCommands(this, chatId);

        String lower = trimmed.toLowerCase();
        // Accept messages that mention grams in Arabic
        if (!lower.contains("جرام") && !lower.contains("غرام")) {
            // not a sell-grams Arabic message — ignore
            sendText(chatId, "من فضلك أرسل الكمية بالأرقام مع كلمة جرام، مثال: ابي ابيع 5 جرام");
            return;
        }

        // extract first number (supports Arabic-Indic digits)
        Matcher m = NUMBER_PATTERN.matcher(lower);
        if (!m.find()) {
            sendText(chatId, "من فضلك أرسل الكمية بالأرقام مع كلمة جرام، مثال: 5 جرام أو ٥ جرام");
            return;
        }

        String raw = m.group(1);
        // Normalize Arabic-Indic digits and common separators to dot
        String normalized = normalizeArabicNumbers(raw).replace('\u060C', '.').replace('،', '.').replace('٫', '.').replace(',', '.');

        double grams;
        try {
            grams = Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            sendText(chatId, "لم أفهم الكمية. أرسل عدد الجرامات رقمياً، مثال: 5 جرام");
            return;
        }

        List<MetalCfdResult> latest = priceService.getLatestPrices();
        String sellPrice = GoldCalculator.calculateCurrentSellGold(latest,grams);
        sendText(chatId, sellPrice);
    }

    // Normalize Arabic-Indic digits to ASCII digits, keep other chars
    private static String normalizeArabicNumbers(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= '\u0660' && c <= '\u0669') { // Arabic-Indic
                sb.append((char) ('0' + (c - '\u0660')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void sendText(Long chatId, String text) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText(text);
            execute(msg);
        } catch (Exception e) {
            logger.error("Failed to send message to chat {}", chatId, e);
        }
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
