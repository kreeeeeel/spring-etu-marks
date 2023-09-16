package com.etu.schedule.telegram;

import com.etu.schedule.entity.GroupEntity;
import com.etu.schedule.repository.GroupRepository;
import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.telegram.util.ScheduleUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static com.etu.schedule.ScheduleApplication.DAY;
import static com.etu.schedule.ScheduleApplication.DAY_FROM_ETU;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String username;

    private final Map<String, TelegramHandler> handler = new HashMap<>();

    private final ScheduleService scheduleService;
    private final GroupRepository groupRepository;


    public TelegramBot(
            @Value("${bot.token}") String token,
            ScheduleService scheduleService,
            List<TelegramHandler> handler,
            GroupRepository groupRepository) {
        super(token);

        this.scheduleService = scheduleService;
        this.groupRepository = groupRepository;

        try {
            this.execute(new SetMyCommands(
                    handler.stream()
                            .filter(it -> it.getDescription() != null)
                            .map(it -> new BotCommand("/" + it.getCommand(), it.getDescription())).toList(),
                    new BotCommandScopeDefault(),
                    null));
        } catch (TelegramApiException e) {
            log.error("Exception for add commands: " + e.getMessage());
        }
        handler.forEach(it -> this.handler.put(it.getCommand(), it));
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void notifySchedule() {
        Integer week = scheduleService.getWeek();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String day = DAY_FROM_ETU.get(index);
        groupRepository.getGroupForNotify().forEach(group -> {

            String title = "\uD83D\uDCDA Группа: " + group.getGroupEtu() + System.lineSeparator() + "⌛ " + DAY.get(index) + (week == 2 ? " Чётная" : " Нечётная")
                    + System.lineSeparator() + System.lineSeparator();

            StringBuilder stringBuilder = new StringBuilder(title);
            scheduleService.getLessons(group.getGroupEtu()).get(day).stream()
                    .filter(it -> it.getAuditoriumReservation().getReservationTime().getWeek().equals(week.toString()))
                    .sorted(Comparator.comparingInt(entry -> entry.getAuditoriumReservation().getReservationTime().getStartTime()))
                    .forEach(it -> stringBuilder.append(ScheduleUtil.getLessonMessage(it)).append(System.lineSeparator()));

            send(group.getTelegramId(), stringBuilder.toString());
        });
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()){

            TelegramHandler handler = this.handler.values().stream()
                    .filter(it -> it.getCallback() != null && it.getCallback().equals(update.getCallbackQuery().getData()))
                    .findFirst()
                    .orElse(null);

            if (handler == null){
                return;
            }

            EditMessageText editMessage = handler.editMessageFromCallback(update.getCallbackQuery());
            if (editMessage != null) execute(editMessage);

        }

        if (update.hasMessage() && update.getMessage().hasText()) {

            String message = update.getMessage().getText().substring(1).replace("@" + username, "").trim();
            Long userId = update.getMessage().getFrom().getId();
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();

            if (!userId.equals(chatId) && !groupRepository.existsByTelegramId(chatId)) {
                groupRepository.save(GroupEntity.builder().telegramId(chatId).build());
            }

            TelegramHandler handler = this.handler.get(message.split(" ")[0]);
            if (handler == null){
                return;
            }
            Pair<String, Boolean> preCommand = handler.preCommand(update);
            if (preCommand != null && preCommand.getLeft() != null) reply(messageId, null, chatId, preCommand.getLeft());
            if (preCommand == null || preCommand.getRight()) reply(messageId, handler.getInlineKeyboard(), chatId, handler.postCommand(update));
        }

    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @SneakyThrows
    public void reply(Integer messageId, InlineKeyboardMarkup keyboard, Long id, String message) {
        execute(
                SendMessage.builder()
                        .chatId(id)
                        .replyToMessageId(messageId)
                        .parseMode("html")
                        .replyMarkup(keyboard)
                        .text(message)
                        .build()
        );
    }

    @SneakyThrows
    public void send(Long id, String message) {
        execute(SendMessage.builder().chatId(id).parseMode("html").text(message).build());
    }

}