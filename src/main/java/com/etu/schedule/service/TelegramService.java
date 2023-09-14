package com.etu.schedule.service;

import org.apache.commons.lang3.tuple.Pair;

public interface TelegramService {
    String changeNotify(Long chatId);
    String getScheduleWeek(String group, Long userId, Long chatId, boolean next);
    String getScheduleDay(String group, Long userId, Long chatId, boolean next);
    Pair<String, Boolean> isValidGroup(String group, Long userId, Long chatId);
    String setGroup(String group, Long userId, Long chatId);
    String getPair(String group, Long userId, Long chatId, boolean next);
    Pair<String, Boolean> isValidGroup(String group);

}
