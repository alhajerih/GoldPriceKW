package com.alhajri.goldPrice.util;

import com.alhajri.goldPrice.entity.MetalCfdResult;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class UtilityClass {

    private static final ZoneId KUWAIT_ZONE = ZoneId.of("Asia/Kuwait");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a", Locale.forLanguageTag("ar-KW"));

    // Build the full message
    public static String buildGoldPriceMessage(List<MetalCfdResult> prices, double lastCfd) {
        if (prices == null || prices.isEmpty()) return "";

        MetalCfdResult first = prices.getFirst();
        double currentCfd = first.getCfdPriceUSD();
        double buyPrice = first.getBuyPrice24KWD().doubleValue();

        String trend = calculateTrend(currentCfd, lastCfd);

        String now = ZonedDateTime.now(KUWAIT_ZONE).format(FORMATTER);

        return new StringBuilder()
                .append("🟡 أسعار الذهب المباشرة 🟡\n\n")
                .append("🇰🇼 سعر الذهب بالدينار الكويتي\n")
                .append("─────────────────────────\n")
                .append("عيار 24: ").append(String.format("%.3f", buyPrice)).append(" د.ك\n")
                .append("─────────────────────────\n\n")
                .append("💱 سعر العقود الآجلة (دولار/أونصة)\n")
                .append("─────────────────────────\n")
                .append(String.format("%.2f", currentCfd)).append(" دولار\n")
                .append("─────────────────────────\n\n")
                .append("📊 اتجاه السوق: ").append(trend).append("\n\n")
                .append("⏰ آخر تحديث: ").append(now).append("\n بتوقيت الكويت")
                .toString();
    }

    // Determine trend
    public static String calculateTrend(double current, double lastCfd) {
        if (current > lastCfd) return " صاعد 📈";
        if (current < lastCfd) return " هابط 📉";
        return " مستقر ➖";
    }
}
