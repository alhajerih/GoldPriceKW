package com.alhajri.goldPrice.DAO;

import com.alhajri.goldPrice.DTO.WhatsAppTextMessageDto;
import com.alhajri.goldPrice.config.TwilioProperties;
import com.alhajri.goldPrice.config.WhatsAppProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.Collections;

@Service
public class WhatsAppService {

    private final WhatsAppProperties waProperties;
    private final TwilioProperties twilioProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public WhatsAppService(
            WhatsAppProperties waProperties,
            TwilioProperties twilioProperties
    ) {
        this.waProperties = waProperties;
        this.twilioProperties = twilioProperties;
    }

    // 🔐 Initialize Twilio
    @PostConstruct
    private void initTwilio() {
        Twilio.init(
                twilioProperties.getAccountSid(),
                twilioProperties.getAuthToken()
        );
    }

    // =============================
    // ✅  TWILIO METHOD
    // =============================
    public void sendMessage(String message) {
        Message.creator(
                new PhoneNumber("whatsapp:" + twilioProperties.getWhatsappTo()),
                new PhoneNumber("whatsapp:" + twilioProperties.getWhatsappFrom()),
                message
        ).create();
    }

    // =============================
    // ✅ WhatsApp Cloud API
    // =============================
    public void sendWhatsAppTextMessage(String message) {
        WhatsAppTextMessageDto dto = new WhatsAppTextMessageDto();
        dto.setTo(twilioProperties.getWhatsappTo().replace("+", ""));
        dto.setText(new WhatsAppTextMessageDto.Text(message));
        String url = String.format(
                "https://graph.facebook.com/%s/%s/messages",
                waProperties.getApiVersion(),
                waProperties.getPhoneNumberId()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", waProperties.getAccessToken().trim());
        HttpEntity<WhatsAppTextMessageDto> request =
                new HttpEntity<>(dto, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                request,
                String.class
        );
    }
}
