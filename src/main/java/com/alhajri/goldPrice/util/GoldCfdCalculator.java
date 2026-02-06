package com.alhajri.goldPrice.util;

public class GoldCfdCalculator {

    private static final double GRAM_TO_OUNCE = 31.1035;
    private static final double BROKER_FACTOR = 0.994;

    /**
     * Calculate CFD using dynamic USD per KWD
     */
    public static long toCfd(double buyPrice24, double usdPerKwd) {
        return Math.round(buyPrice24 * usdPerKwd * GRAM_TO_OUNCE * BROKER_FACTOR);
    }
}


