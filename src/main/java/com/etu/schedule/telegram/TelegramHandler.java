package com.etu.schedule.telegram;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramHandler {
    String getCommand();
    String getCallback();
    String getDescription();
    EditMessageText editMessageFromCallback(CallbackQuery callbackQuery);
    InlineKeyboardMarkup getInlineKeyboard();
    Pair<String, Boolean> preCommand(Update update);
    String postCommand(Update update);
}
