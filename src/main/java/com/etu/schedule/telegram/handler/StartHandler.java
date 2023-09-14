package com.etu.schedule.telegram.handler;

import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.telegram.TelegramHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartHandler implements TelegramHandler {

    private final ScheduleService scheduleService;

    @Override
    public String getCommand() {
        return "start";
    }

    @Override
    public String getDescription() {
        return null;
    }

    public String getCallback() {
        return "how is working";
    }

    @Override
    public Pair<String, Boolean> preCommand(Update update) {
        return null;
    }

    @Override
    public String postCommand(Update update) {
        return String.format("""
                <b>👽 Бо-би-бо-бааа</b>
                👋 Добро пожаловать!
                                
                Я готов предоставить возможности:
                👨‍🏫 Информация о преподавателях
                👀 Расписание
                ✍️ Заметки
                🧠 Отмечаться за вас
                                
                Доступно расписание для <b>%d</b> групп.
                               
                """, scheduleService.getCountGroup());
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Как работает?").callbackData("how is working").build())
                )
                .build();
    }

    @Override
    public EditMessageText editMessageFromCallback(CallbackQuery callbackQuery) {
        return EditMessageText.builder()
                .messageId(callbackQuery.getMessage().getMessageId())
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                        <b>😈 Авторизация</b>
                        Для авторизации вам нужно воспользоваться командой /auth. Бот войдет в ваш аккаунт и получит ваше ФИО и номер группы.
                                                
                        <b>🧠 Как работают отметки?</b>
                        Чтобы бот отмечал вас, вам нужно авторизоваться. После авторизации бот во время вашей пары будет заходить в ваш аккаунт и отмечать вас. Если не нужно отмечаться на некоторых парах, это можно настроить с помощью команды /note.
                                                
                        <b>✌️ Расписание</b>
                        После авторизации вам будет присвоена ваша группа, и вы сможете полностью использовать доступные команды. Если вы хотите добавить бота в группу, то просто используйте команду /group, и бот будет оповещать вас в 06:00 о предстоящих парах. Это также можно отключить командой /notify.
                                                
                        Если вы не хотите авторизовываться, вы всегда можете просто сменить группу с помощью команды /group.
                        
                        ⚠️ Но если вы авторизованны то бот всё равно будет ходить и отмечать вас по группе с личного кабинет!
                                                
                        """)
                .parseMode("html")
                .build();
    }

}
