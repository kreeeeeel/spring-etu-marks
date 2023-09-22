package com.etu.schedule.service;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramService {

    String changeNote(Long userId);
    String changeNotify(Long chatId);
    InlineKeyboardMarkup getInlineMarkupNote(Long userId);
    String getScheduleWeek(String group, Long userId, Long chatId, boolean next);
    String getScheduleDay(String group, Long userId, Long chatId, boolean next);
    Pair<String, Boolean> isValidGroup(String group, Long userId, Long chatId);
    String setGroup(String group, Long userId, Long chatId);
    String getPair(String group, Long userId, Long chatId, boolean next);
    Pair<String, Boolean> isValidGroup(String group);
    String getMessageReplaced(String message);

}
