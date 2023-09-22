package com.etu.schedule.service.impl;

import com.etu.schedule.entity.GroupEntity;
import com.etu.schedule.entity.UserEntity;
import com.etu.schedule.entry.LessonDayEntry;
import com.etu.schedule.entry.LessonEntry;
import com.etu.schedule.entry.ScheduleEntry;
import com.etu.schedule.repository.GroupRepository;
import com.etu.schedule.repository.UserRepository;
import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.service.TelegramService;
import com.etu.schedule.telegram.util.ScheduleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    @Value("${bot.username}")
    private String username;

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public static final List<String> TIME = List.of("08:00", "09:50", "11:40", "13:40", "15:30", "17:20", "19:05", "20:50");
    public static final List<String> DAY = List.of("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота");

    public Pair<String, Boolean> isValidGroup(String group) {
        if (group.isEmpty()) {
            return Pair.of("""
                    \uD83D\uDE3F Укажите номер группы..
                    Формат: /group номер
                    """, false);
        }

        if (group.length() != 4)
            return Pair.of("\uD83D\uDE3F Некорректное значение группы..", false);

        if (scheduleService.isExistGroup(group))
            return Pair.of("\uD83D\uDE3F Такой группы не существует..", false);

        return Pair.of(null, true);
    }

    public String setGroup(String group, Long userId, Long chatId) {
        if (!userId.equals(chatId)) {

            GroupEntity groupEntity = groupRepository.findByTelegramId(chatId)
                    .orElse(GroupEntity.builder().telegramId(chatId).build());

            groupEntity.setGroupEtu(group);
            groupRepository.save(groupEntity);

            return "\uD83D\uDE03 В чате установлена " + group + " группа.";
        }

        UserEntity userEntity = userRepository.findByTelegramId(userId)
                .orElse(UserEntity.builder().telegramId(userId).build());

        userRepository.save(userEntity);
        return "\uD83D\uDE03 Вы выбрали " + group + " группу.";
    }

    public Pair<String, Boolean> isValidGroup(String group, Long userId, Long chatId) {

        if (group.isEmpty()) {

            if (!chatId.equals(userId) && !groupRepository.existsByTelegramIdAndGroupEtuIsNotNull(chatId)){
                return Pair.of("""
                        \uD83D\uDE3F В чате не установлена группа!
                        
                        Укажите группу: /group номер
                        """, false);
            }

            if (chatId.equals(userId) && !userRepository.existsByTelegramIdAndGroupScheduleIsNotNull(userId)) {
                return Pair.of("""
                        \uD83D\uDE3F У вас нет группы
                        Укажите группу: /group номер>
                        """, false);
            }
        }
        if (!group.isEmpty()) {
            if (group.length() != 4)
                return Pair.of("\uD83D\uDE3F Некорректное значение группы..", false);

            if (scheduleService.isExistGroup(group))
                return Pair.of("\uD83D\uDE3F Такой группы не существует..", false);
        }
        return Pair.of(null, true);

    }

    public String getPair(String group, Long userId, Long chatId, boolean next) {

        group = getGroup(group, userId, chatId);
        LessonEntry lessonCurrent = scheduleService.getLessonCurrent(group, next);
        if (lessonCurrent == null){
            return "\uD83E\uDD73 " + (next ? "Следующей пары нету, расслабься" : "Сейчас нет пары, чил");
        }

        return String.format("""
                📌 %s пара:
                
                %s - %s (%s)%s%s
                """,
                next ? "Следующая" : "Текущая",
                TIME.get(lessonCurrent.getPair()),
                lessonCurrent.getShortName(),
                lessonCurrent.getType(),
                lessonCurrent.getAuditorium() != null ? " ауд." + lessonCurrent.getAuditorium() : "",
                lessonCurrent.getTeacher() != null ? System.lineSeparator() + " - " + lessonCurrent.getTeacher() : ""
        );
    }

    @Override
    public String changeNote(Long userId) {
        UserEntity userEntity = userRepository.findByTelegramId(userId)
                .orElse(UserEntity.builder().telegramId(userId).build());

        if (userEntity.getEmail() == null || userEntity.getPassword() == null){
            return "❌ Вы не были авторизованы! /auth";
        }

        userEntity.setNote(!userEntity.isNote());
        userRepository.save(userEntity);
        return userEntity.isNote() ? "\uD83D\uDE00 Отлично! Теперь бот будет ходить и отмечаться за вас!" : "\uD83D\uDE2C Бот больше не будет за вас отмечаться..";
    }

    @Override
    public String changeNotify(Long chatId) {
        GroupEntity groupEntity = groupRepository.findByTelegramId(chatId)
                .orElse(GroupEntity.builder().telegramId(chatId).build());

        groupEntity.setNotify(!groupEntity.isNotify());
        groupRepository.save(groupEntity);
        return groupEntity.isNotify() ? """
                \uD83D\uDCD2 В беседе включена рассылка о расписании!
                Каждый день в 06:00 в беседу будет отправлять текущее расписание.""" : "\uD83D\uDCD2 В беседе была отключена рассылка о расписании.";
    }

    @Override
    public InlineKeyboardMarkup getInlineMarkupNote(Long userId) {
        return null;
    }

    public String getScheduleWeek(String group, Long userId, Long chatId, boolean next) {

        group = getGroup(group, userId, chatId);
        ScheduleEntry scheduleEntry = scheduleService.getLessonWeek(group, next);

        StringBuilder stringBuilder = new StringBuilder(String.format("""
                \uD83D\uDCDA %s
                Группа: %s
                """,
                (scheduleEntry.getWeek() == 2 ? "Чётная " : "Нечётная ") + "неделя",
                scheduleEntry.getGroup()
        ));

        scheduleEntry.getEntry().forEach(it -> {
            stringBuilder.append(String.format("""
                    
                    📌 %s
                    """, DAY.get(it.getDay())));

            it.getLesson().forEach(th -> stringBuilder.append(ScheduleUtil.getLessonMessage(th)).append(System.lineSeparator()));
        });
        return stringBuilder.toString();
    }

    public String getScheduleDay(String group, Long userId, Long chatId, boolean next) {

        group = getGroup(group, userId, chatId);
        LessonDayEntry lessonDay = scheduleService.getLessonDay(group, next);
        if (lessonDay == null){
            return "📌 " + (next ? "Завтра выходной." : "Сегодня отдыхаем");
        }

        StringBuilder stringBuilder = new StringBuilder(String.format("""
                \uD83D\uDCDA Группа: %s
                ⌛ %s : %s
                
                """,
                group,
                DAY.get(lessonDay.getDay()),
                lessonDay.getWeek() == 2 ? " Чётная" : " Нечётная"
        ));

        lessonDay.getLesson().forEach(it -> stringBuilder.append(ScheduleUtil.getLessonMessage(it)).append(System.lineSeparator()));
        return stringBuilder.toString();
    }

    @Override
    public String getMessageReplaced(String message) {
        return message.replace("@" + username, "");
    }

    private String getGroup(String group, Long userId, Long chatId){
        if (group.isEmpty()) {
            group = userId.equals(chatId) ? userRepository.getGroupByUser(userId) : groupRepository.getGroupByChat(chatId);
        }
        return group;
    }

}
