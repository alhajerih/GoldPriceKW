package com.alhajri.goldPrice.services;

import com.alhajri.goldPrice.DAO.MetalPriceDaoImpl;
import com.alhajri.goldPrice.config.WhatsAppService;
import com.alhajri.goldPrice.entity.MetalCfdResult;
import lombok.Getter;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveMetalPriceService {

    private final MetalPriceDaoImpl metalPriceDao;
    private final ScheduledExecutorService scheduler;

    // Thread-safe latest prices
    @Getter
    private volatile List<MetalCfdResult> latestPrices;

    // Thread-safe latest FX
    private volatile double usdPerKwd;
    private final WhatsAppService whatsAppService;
    // Track last CFD sent for each metal
    private final Map<Integer, Long> lastSentCfd = new HashMap<>();//store CFD that sent
    private final long threshold = 100; // send if CFD changes >= 100 USD

    public LiveMetalPriceService(MetalPriceDaoImpl metalPriceDao, WhatsAppService whatsAppService) {
        this.metalPriceDao = metalPriceDao;
        this.whatsAppService = whatsAppService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.usdPerKwd = metalPriceDao.getUsdPerKwd(); // initial FX
    }

    // Start polling metals and FX
    public void start() {
        // Poll metals every 15s
        scheduler.scheduleAtFixedRate(this::updateMetalPrices, 0, 15, TimeUnit.SECONDS);
        // Poll FX every 60s
        scheduler.scheduleAtFixedRate(this::updateFxRate, 0, 60, TimeUnit.SECONDS);
    }

    // Stop polling
    public void stop() {
        scheduler.shutdownNow();
    }

    // Update FX rate
    private void updateFxRate() {
        try {
            double newFx = metalPriceDao.getUsdPerKwd();
            if (newFx > 0) {
                usdPerKwd = newFx;
                System.out.println("FX updated: USD/KWD = " + usdPerKwd);
            }
        } catch (Exception e) {
            System.err.println("Failed to refresh FX: " + e.getMessage());
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
                        // Build message string
                        StringBuilder msg = new StringBuilder("Live Gold Prices:\n");
                            long lastCfd = lastSentCfd.getOrDefault(prices.getFirst().getMetalType(), 0L);
                            if (Math.abs(prices.getFirst().getCfdPriceUSD() - lastCfd) >= threshold) {
                                msg.append("KWD/gram=").append(prices.getFirst().getBuyPrice24KWD())
                                        .append("\n")
                                        .append("CFD USD/oz=").append(prices.getFirst().getCfdPriceUSD());

                                // update last sent
                                lastSentCfd.put(prices.getFirst().getMetalType(), prices.getFirst().getCfdPriceUSD());
                                whatsAppService.sendMessage(msg.toString());
                            }
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Failed to schedule metal price update: " + e.getMessage());
        }
    }
}
