package com.alhajri.goldPrice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "telegram.bot")
@Setter
@Getter
public class TelegramProperties {
    @NotBlank(message = "telegram.bot.bot-name must not be blank")
    private String botName;

    @NotBlank(message = "telegram.bot.bot-token must not be blank")
    private String botToken;
}
