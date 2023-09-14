package com.etu.schedule.telegram.handler;

import com.etu.schedule.service.TelegramService;
import com.etu.schedule.telegram.TelegramHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class GroupHandler implements TelegramHandler {

    private final TelegramService telegramService;

    private final String GROUP_COMMAND = "group";

    @Override
    public InlineKeyboardMarkup getInlineKeyboard() {
        return null;
    }

    @Override
    public String getCallback() {
        return null;
    }

    @Override
    public EditMessageText editMessageFromCallback(CallbackQuery callbackQuery) {
        return null;
    }

    @Override
    public String getCommand() {
        return GROUP_COMMAND;
    }

    @Override
    public String getDescription() {
        return "Сменить группу в чате";
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {
        return telegramService.isValidGroup(update.getMessage().getText().substring(("/" + GROUP_COMMAND).length()).trim());
    }

    @Override
    public String postCommand(Update update) {
        return telegramService.setGroup(
                update.getMessage().getText().substring(("/" + GROUP_COMMAND).length()).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId()
        );
    }
}