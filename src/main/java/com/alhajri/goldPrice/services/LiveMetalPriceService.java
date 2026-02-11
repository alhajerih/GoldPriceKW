package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.DAO.MetalPriceDaoImpl;
import com.alhajri.goldPrice.DAO.WhatsAppService;
import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.util.GoldCfdCalculator;
import com.alhajri.goldPrice.util.UtilityClass;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Thread-safe latest prices
    @Getter
    private volatile List<MetalCfdResult> latestPrices;

    // Thread-safe latest FX
    private volatile double usdPerKwd;
    // Track last CFD sent for each metal
    private final Map<Integer, Long> lastSentCfd = new HashMap<>();//store CFD that sent
    private final long threshold = 25; // send if CFD changes >= 100 USD

    public LiveMetalPriceService(MetalPriceDaoImpl metalPriceDao, WhatsAppService whatsAppService, BotService botService, ApplicationContext applicationContext) {
        this.metalPriceDao = metalPriceDao;
        this.whatsAppService = whatsAppService;
        this.botService = botService;
        this.applicationContext = applicationContext;
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
                        prices.forEach(p -> p.setCfdPriceUSD(GoldCfdCalculator.toCfd(
                                        p.getBuyPrice24KWD().doubleValue(),
                                        usdPerKwd
                                )
                        ));

                        latestPrices = prices;
                        long lastCfd = lastSentCfd.getOrDefault(prices.getFirst().getMetalType(), 0L);
                        if (Math.abs(prices.getFirst().getCfdPriceUSD() - lastCfd) >= threshold||lastSentCfd.isEmpty()) {
                            // update last sent CFD Price

                                // =============================
                                // ✅ TELEGRAM API Call
                                // =============================
                                TelegramBot telegramBot = getTelegramBot();
                                if (telegramBot != null) {
                                    int activeChatCount = botService.getActiveChatIds().size();
                                    if (activeChatCount > 0) {
                                        logger.info("✅ TELEGRAM: Broadcasting price update to {} chat(s)", activeChatCount);
                                        botService.broadcastToAllChats(telegramBot, UtilityClass.buildGoldPriceMessage(prices, lastCfd));
                                        lastSentCfd.put(prices.getFirst().getMetalType(), prices.getFirst().getCfdPriceUSD());
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
                            }
                    })
                    .exceptionally(ex -> {
                        logger.error("Error in price update callback", ex);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to schedule metal price update: {}", e.getMessage());
        }
    }
}
