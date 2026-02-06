package com.alhajri.goldPrice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "twilio")
@Setter
@Getter
public class TwilioProperties {

    private String accountSid;
    private String authToken;
    private String whatsappFrom;
    private String whatsappTo;
}
