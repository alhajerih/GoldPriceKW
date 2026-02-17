package com.alhajri.goldPrice.util;

import com.alhajri.goldPrice.entity.MetalCfdResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class GoldCalculator {

    private static final double GRAM_TO_OUNCE = 31.1035;
    private static final double BROKER_FACTOR = 0.993;

    /**
     * Calculate CFD using dynamic USD per KWD
     */
    public static long toCfd(double buyPrice24, double usdPerKwd) {
        return Math.round(buyPrice24 * usdPerKwd * GRAM_TO_OUNCE * BROKER_FACTOR);
    }

    public static String calculateCurrentSellGold(List<MetalCfdResult> latest, double grams) {

        String reply;
        if (latest == null || latest.isEmpty()) {
            reply= "عذراً، لا يتوفر السعر الحالي الآن. حاول مرة أخرى لاحقاً.";
            return reply;
        }
        BigDecimal pricePerGram = latest.getFirst().getBuyPrice24KWD();
        String perGramStr = pricePerGram.setScale(3, RoundingMode.HALF_UP).toPlainString();
        pricePerGram =pricePerGram.subtract(pricePerGram.multiply(BigDecimal.valueOf(0.007115)));
        BigDecimal total = pricePerGram.multiply(BigDecimal.valueOf(grams)).setScale(3, RoundingMode.HALF_UP);
        String gramsStr = BigDecimal.valueOf(grams).stripTrailingZeros().toPlainString();
        return String.format("سعر البيع لـ %s جرام = %s د.ك\n(سعر 1 جرام = %s د.ك)", gramsStr, total.toPlainString(), perGramStr);
    }
}


