package com.alhajri.goldPrice.DTO;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class TeleChatDTO {

    private String text;
    private boolean removeKeyboard = false;
    private List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
    private List<KeyboardRow> replyButtons = new ArrayList<>();

    public static TeleChatDTO create() {
        return new TeleChatDTO();
    }

    public TeleChatDTO text(String text) {
        this.text = text;
        return this;
    }

    public TeleChatDTO removeKeyboard() {
        this.removeKeyboard = true;
        return this;
    }

    public TeleChatDTO inlineButton(String label, String data) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(label).callbackData(data).build());
        inlineButtons.add(row);
        return this;
    }

    public TeleChatDTO inlineButtonRow(String... labelsAndData) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < labelsAndData.length; i += 2) {
            String label = labelsAndData[i];
            String data = labelsAndData[i + 1];
            row.add(InlineKeyboardButton.builder().text(label).callbackData(data).build());
        }

        inlineButtons.add(row);
        return this;
    }

    public TeleChatDTO replyButton(String label) {
        KeyboardRow row = new KeyboardRow();
        row.add(label);
        replyButtons.add(row);
        return this;
    }

    public TeleChatDTO replyButtonRow(String... labels) {
        KeyboardRow row = new KeyboardRow();
        for (String label : labels) row.add(label);
        replyButtons.add(row);
        return this;
    }
    public TeleChatDTO replyContactButton(String label) {
        KeyboardButton btn = new KeyboardButton(label);
        btn.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(btn);
        replyButtons.add(row);

        return this;
    }


    public SendMessage build(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);

        // Inline buttons
        if (!inlineButtons.isEmpty()) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(inlineButtons);
            msg.setReplyMarkup(markup);
        }

        // Reply buttons
        if (!replyButtons.isEmpty()) {
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(replyButtons);
            markup.setResizeKeyboard(true);
            msg.setReplyMarkup(markup);
        }

        // Remove keyboard
        if (removeKeyboard) {
            msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        }

        return msg;
    }
}
