package com.alhajri.goldPrice.config;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class WhatsAppService {

    private String from="+14155238886";
    private String to="+965########";

    public WhatsAppService() {
        // Twilio Sandbox credentials
        String accountSid1 = "#####################";
        String authToken1 = "#####################";
        Twilio.init(accountSid1, authToken1);
        this.from = from;
        this.to = to;
    }

    public void sendMessage(String message) {
        Message.creator(
                new com.twilio.type.PhoneNumber("whatsapp:" + to),
                new com.twilio.type.PhoneNumber("whatsapp:" + from),
                message
        ).create();
    }
}

