package com.alhajri.goldPrice.controller;

import com.alhajri.goldPrice.services.BotService;
import com.alhajri.goldPrice.services.TelegramBot;
import com.alhajri.goldPrice.repository.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {
    private final BotService botService;
    private final TelegramUserRepository repo;

    // TelegramBot is optional: app should still run even if bot failed to initialize
    @Autowired(required = false)
    private TelegramBot telegramBot;

    public AdminController(BotService botService, TelegramUserRepository repo) {
        this.botService = botService;
        this.repo = repo;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", repo.findAll());
        return "admin";
    }

    @PostMapping("/admin/send")
    public String sendMessage(@RequestParam(required = false) String chatId,
                              @RequestParam String message,
                              Model model) {
        Long parsedChatId = null;
        if (chatId != null && !chatId.trim().isEmpty()) {
            try {
                parsedChatId = Long.parseLong(chatId.trim());
            } catch (NumberFormatException ex) {
                model.addAttribute("sent", "معرف المحادثة غير صالح");
                model.addAttribute("users", repo.findAll());
                return "admin";
            }
        }

        if (parsedChatId == null) {
            if (telegramBot == null) {
                model.addAttribute("sent", "خطأ: بوت التليجرام غير متصل حالياً.");
            } else {
                botService.broadcastToAllChats(telegramBot, message);
                model.addAttribute("sent", "بُعثت الرسالة لكل المستخدمين");
            }
        } else {
            // send to specific
            if (telegramBot == null) {
                model.addAttribute("sent", "خطأ: بوت التليجرام غير متصل حالياً.");
            } else {
                try {
                    telegramBot.sendText(parsedChatId, message);
                    model.addAttribute("sent", "بُعثت الرسالة للمستخدم: " + parsedChatId);
                } catch (Exception e) {
                    model.addAttribute("sent", "فشل الإرسال: " + e.getMessage());
                }
            }
        }
        model.addAttribute("users", repo.findAll());
        return "admin";
    }
}
