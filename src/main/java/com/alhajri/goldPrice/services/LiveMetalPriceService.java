package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.DAO.MetalPriceDaoImpl;
import com.alhajri.goldPrice.DAO.WhatsAppService;
import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.util.GoldCalculator;
import com.alhajri.goldPrice.util.UtilityClass;
import com.alhajri.goldPrice.repository.TelegramUserRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
public class LiveMetalPriceService {
    private static final Logger logger = LoggerFactory.getLogger(LiveMetalPriceService.class);

    private final MetalPriceDaoImpl metalPriceDao;
    private final ScheduledExecutorService scheduler;
    private final BotService botService;
    private final ApplicationContext applicationContext;
    private final WhatsAppService whatsAppService;
    private final TelegramUserRepository telegramUserRepository;

    // Thread-safe latest prices
    @Getter
    private volatile List<MetalCfdResult> latestPrices;

    // Thread-safe latest FX
    private volatile double usdPerKwd;
    // Threshold: send notification if CFD changes >= this value
    private final long threshold = 25;

    public LiveMetalPriceService(MetalPriceDaoImpl metalPriceDao,
                                 WhatsAppService whatsAppService,
                                 BotService botService,
                                 ApplicationContext applicationContext,
                                 TelegramUserRepository telegramUserRepository) {
        this.metalPriceDao = metalPriceDao;
        this.whatsAppService = whatsAppService;
        this.botService = botService;
        this.applicationContext = applicationContext;
        this.telegramUserRepository = telegramUserRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.usdPerKwd = metalPriceDao.getUsdPerKwd(); // initial FX
    }

    // Start polling metals and FX
    public void start() {
        logger.info("Starting LiveMetalPriceService polling...");
        // Poll metals every 15s
        scheduler.scheduleAtFixedRate(this::updateMetalPrices, 0, 15, TimeUnit.SECONDS);
        // Poll FX every 60s
        scheduler.scheduleAtFixedRate(this::updateFxRate, 0, 60, TimeUnit.SECONDS);
    }

    // Stop polling
    public void stop() {
        logger.info("Stopping LiveMetalPriceService polling...");
        scheduler.shutdownNow();
    }

    // Update FX rate
    private void updateFxRate() {
        try {
            double newFx = metalPriceDao.getUsdPerKwd();
            if (newFx > 0) {
                usdPerKwd = newFx;
            }
        } catch (Exception e) {
            logger.error("Failed to refresh FX: {}", e.getMessage());
        }
    }

    // Get TelegramBot lazily to avoid circular dependency
    private TelegramBot getTelegramBot() {
        try {
            return applicationContext.getBean(TelegramBot.class);
        } catch (Exception e) {
            logger.warn("TelegramBot not available yet: {}", e.getMessage());
            return null;
        }
    }

    // Update metal prices + CFD using async call
    private void updateMetalPrices() {
        try {
            metalPriceDao.getMetalPricesWithCfdAsync()
                    .thenAccept(prices -> {
                        // Recalculate CFD with latest FX
                        prices.forEach(p -> p.setCfdPriceUSD(GoldCalculator.toCfd(
                                        p.getBuyPrice24KWD().doubleValue(),
                                        usdPerKwd
                                )
                        ));

                        latestPrices = prices;
                        long currentCfd = prices.get(0).getCfdPriceUSD();

                        // =============================
                        // ✅ TELEGRAM API Call (per-user lastSentCfd)
                        // =============================
                        TelegramBot telegramBot = getTelegramBot();
                        if (telegramBot != null) {
                            var chatIds = botService.getActiveChatIds();
                            int activeChatCount = chatIds.size();
                            if (activeChatCount > 0) {
                                logger.info("✅ TELEGRAM: Checking {} user(s) for price updates", activeChatCount);
                                int sentCount = 0;
                                for (Long chatId : chatIds) {
                                    // Get user's last sent CFD from DB
                                    var telegramUser = telegramUserRepository.findById(chatId).orElse(null);
                                    long userLastCfd = (telegramUser != null) ? telegramUser.getLastSentCfd() : 0L;

                                    // Check if threshold exceeded for this user
                                    if (Math.abs(currentCfd - userLastCfd) >= threshold) {
                                        try {
                                            String message = UtilityClass.buildGoldPriceMessage(prices, userLastCfd);
                                            telegramBot.sendText(chatId, message);
                                            botService.updateLastSentCfd(chatId, currentCfd);
                                            sentCount++;
                                            logger.debug("  ✓ Sent price update to chat {}", chatId);
                                        } catch (Exception e) {
                                            logger.error("  ✗ Failed to send to chat {}: {}", chatId, e.getMessage());
                                            botService.removeChatId(chatId);
                                        }
                                    } else {
                                        logger.debug("  ⊘ Chat {} within threshold (current: {}, last: {})",
                                            chatId, currentCfd, userLastCfd);
                                    }
                                }
                                logger.info("✅ TELEGRAM: Sent updates to {}/{} user(s)", sentCount, activeChatCount);
                            } else {
                                logger.info("📭 TELEGRAM: No active chats (waiting for users to message bot)");
                            }
                        } else {
                            logger.warn("⚠️  TELEGRAM: Bot bean not available");
                        }

                        // =============================
                        // ✅ TWILIO API Call
                        // =============================
                        logger.info("📞 WHATSAPP (Twilio): Sending Not message...");
                        //whatsAppService.sendMessage(msg.toString());

                        // =============================
                        // ✅ WhatsApp Cloud API Call
                        // =============================
                        logger.info("📱 WHATSAPP (Cloud API): Sending Not message...");
                        //whatsAppService.sendWhatsAppTextMessage(msg.toString());
                    })
                    .exceptionally(ex -> {
                        logger.error("Error in price update callback", ex);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to schedule metal price update: {}", e.getMessage());
        }
    }

    public String getLatestGoldPrices(Long chatId) {
        List<MetalCfdResult> prices = null;
        try {
            prices = metalPriceDao.getMetalPricesWithCfd();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Recalculate CFD with latest FX
        prices.forEach(p -> p.setCfdPriceUSD(GoldCalculator.toCfd(
                p.getBuyPrice24KWD().doubleValue(),
                usdPerKwd
        )));

        // Get user's last sent CFD from DB
        var telegramUser = telegramUserRepository.findById(chatId).orElse(null);
        long userLastCfd = (telegramUser != null) ? telegramUser.getLastSentCfd() : 0L;

        logger.info("✅ TELEGRAM: Sending price to user {} (last sent: {})", chatId, userLastCfd);
        return UtilityClass.buildGoldPriceMessage(prices, userLastCfd);
    }
}
