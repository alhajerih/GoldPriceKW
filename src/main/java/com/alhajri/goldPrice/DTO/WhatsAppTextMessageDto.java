package com.alhajri.goldPrice.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppTextMessageDto {

    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";
    private String to;
    private String type = "text";
    private Text text;

    @Setter
    @Getter
    public static class Text {
        private String body;
        public Text(String body) {
            this.body = body;
        }

    }
}
