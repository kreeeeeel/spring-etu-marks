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

import java.util.List;

@Component
@RequiredArgsConstructor
public class NextWeekHandler  implements TelegramHandler {

    private final TelegramService telegramService;

    private final String NEXT_WEEK_COMMAND = "nextweek";

    @Override
    public String getCommand() {
        return NEXT_WEEK_COMMAND;
    }

    @Override
    public String getDescription() {
        return "Расписание на след.неделю";
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard(Long userId) {
        return null;
    }

    @Override
    public List<String> getListCallback() {
        return null;
    }

    @Override
    public EditMessageText editMessageFromCallback(CallbackQuery callbackQuery) {
        return null;
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {
        return telegramService.isValidGroup(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + NEXT_WEEK_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId()
        );
    }

    @Override
    public String postCommand(Update update) {
        return telegramService.getScheduleWeek(
                telegramService.getMessageReplaced(update.getMessage().getText().substring(("/" + NEXT_WEEK_COMMAND).length())).trim(),
                update.getMessage().getFrom().getId(),
                update.getMessage().getChatId(),
                true
        );
    }
}
