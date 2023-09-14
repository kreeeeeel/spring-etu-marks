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
public class NoteHandler implements TelegramHandler {

    private final TelegramService telegramService;

    @Override
    public String getCommand() {
        return "/note";
    }

    @Override
    public String getCallback() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Включить/Отключить отметки";
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
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        if (!userId.equals(chatId)){
            return Pair.of("❌ Команда работает только в личном диалоге!", false);
        }
        return null;
    }

    @Override
    public String postCommand(Update update) {
        return telegramService.changeNote(update.getMessage().getFrom().getId());
    }
}
