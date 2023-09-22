package com.etu.schedule.telegram;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface TelegramHandler {
    String getCommand();
    List<String> getListCallback();
    String getDescription();
    EditMessageText editMessageFromCallback(CallbackQuery callbackQuery);
    InlineKeyboardMarkup getInlineKeyboard(Long userId);
    Pair<String, Boolean> preCommand(Update update);
    String postCommand(Update update);
}
