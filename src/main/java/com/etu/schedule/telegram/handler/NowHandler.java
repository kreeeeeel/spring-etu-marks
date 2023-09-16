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
public class NowHandler implements TelegramHandler {

    private final TelegramService telegramService;

    private final String NOW_COMMAND = "now";

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
        return NOW_COMMAND;
    }

    @Override
    public String getDescription() {
        return "Текущая пара";
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {
        return telegramService.isValidGroup(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + NOW_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId()
        );
    }

    @Override
    public String postCommand(Update update) {
        return telegramService.getPair(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + NOW_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId(),
                false
        );
    }

}
