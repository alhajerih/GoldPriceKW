package com.alhajri.goldPrice.config;

import com.alhajri.goldPrice.services.LiveMetalPriceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfig {

    @Bean
    CommandLineRunner startLivePrices(LiveMetalPriceService liveService) {
        return args -> {
            // Start polling
            liveService.start();
        };
    }
}
