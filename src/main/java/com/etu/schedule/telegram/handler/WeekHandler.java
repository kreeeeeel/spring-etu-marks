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
public class WeekHandler implements TelegramHandler {

    private final TelegramService telegramService;

    private final String WEEK_COMMAND = "week";

    @Override
    public String getCommand() {
        return WEEK_COMMAND;
    }

    @Override
    public String getCallback() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Расписание на неделю";
    }

    @Override
    public EditMessageText editMessageFromCallback(CallbackQuery callbackQuery) {
        return null;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard() {
        return null;
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {
        return telegramService.isValidGroup(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + WEEK_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId()
        );
    }

    @Override
    public String postCommand(Update update) {
        return telegramService.getScheduleWeek(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + WEEK_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId(),
                false
        );
    }
}
