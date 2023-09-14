package com.etu.schedule.service.impl;

import com.etu.schedule.entity.GroupEntity;
import com.etu.schedule.entity.UserEntity;
import com.etu.schedule.entry.PairEntry;
import com.etu.schedule.repository.GroupRepository;
import com.etu.schedule.repository.UserRepository;
import com.etu.schedule.service.ScheduleService;
import com.etu.schedule.service.TelegramService;
import com.etu.schedule.telegram.util.ScheduleUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.etu.schedule.ScheduleApplication.*;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public Pair<String, Boolean> isValidGroup(String group) {
        if (group.isEmpty()) {
            return Pair.of("""
                    \uD83D\uDE3F Укажите номер группы..
                    Формат: /group номер
                    """, false);
        }

        if (group.length() != 4)
            return Pair.of("\uD83D\uDE3F Некорректное значение группы..", false);

        if (!scheduleService.isGroup(group))
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

            if (!scheduleService.isGroup(group))
                return Pair.of("\uD83D\uDE3F Такой группы не существует..", false);
        }
        return Pair.of(null, true);

    }

    public String getPair(String group, Long userId, Long chatId, boolean next) {
        if (group.isEmpty()) {
            group = userId.equals(chatId) ? userRepository.getGroupByUser(userId) : groupRepository.getGroupByChat(chatId);
        }

        String finalGroup = group;
        List<PairEntry> entries = next ? scheduleService.getLessonNext() : scheduleService.getLessonNow();
        if (entries == null){
            return "\uD83E\uDD73 В данный момент нет пар.";
        }

        PairEntry pairEntry = entries.stream()
                .filter(it -> it.getGroup().equals(finalGroup))
                .findFirst()
                .orElse(null);

        if (pairEntry == null || pairEntry.getTeacher() == null){
            return "\uD83E\uDD73 В данный момент нет пар.";
        }
        return String.format("""
                📌 %s пара:
                
                %s - %s (%s)%s%s
                """,
                next ? "Следующая" : "Текущая",
                TIME.get(pairEntry.getPair()),
                pairEntry.getShortTitle(),
                pairEntry.getLessonType(),
                pairEntry.getAuditorium() != null ? " ауд." + pairEntry.getAuditorium() : "",
                System.lineSeparator() + " - " + pairEntry.getTeacher()
        );
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

    public String getScheduleWeek(String group, Long userId, Long chatId, boolean next) {

        if (group.isEmpty()) {
            group = userId.equals(chatId) ? userRepository.getGroupByUser(userId) : groupRepository.getGroupByChat(chatId);
        }

        int week = next ? (scheduleService.getWeek() == 2 ? 1 : 2) : scheduleService.getWeek();
        String title = "\uD83D\uDCDA " + (week == 2 ? "Чётная " : "Нечётная ") + "неделя"
                + System.lineSeparator() + "\uD83D\uDC65 Группа: " + group + System.lineSeparator();

        StringBuilder stringBuilder = new StringBuilder(title);

        scheduleService.getLessons(group).entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> DAY_FROM_ETU.indexOf(entry.getKey())))
                .forEach(lesson -> {
                    stringBuilder.append(System.lineSeparator())
                            .append(String.format("📌 %s", DAY.get(DAY_FROM_ETU.indexOf(lesson.getKey()))))
                            .append(System.lineSeparator());

                    lesson.getValue().stream()
                            .filter(it -> it.getAuditoriumReservation().getReservationTime().getWeek().equals(Integer.toString(week)))
                            .sorted(Comparator.comparingInt(entry -> entry.getAuditoriumReservation().getReservationTime().getStartTime()))
                            .forEach(it -> stringBuilder.append(ScheduleUtil.getLessonMessage(it)).append(System.lineSeparator()));
                });
        return stringBuilder.toString();
    }

    public String getScheduleDay(String group, Long userId, Long chatId, boolean next) {
        Integer week = scheduleService.getWeek();
        if (group.isEmpty()) {
            group = userId.equals(chatId) ? userRepository.getGroupByUser(userId) : groupRepository.getGroupByChat(chatId);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        int index = next ? calendar.get(Calendar.DAY_OF_WEEK) : calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String title = "\uD83D\uDCDA Группа: " + group + System.lineSeparator() + "⌛ " + DAY.get(index) + (week == 2 ? " Чётная" : " Нечётная")
                + System.lineSeparator() + System.lineSeparator();
        StringBuilder stringBuilder = new StringBuilder(title);

        scheduleService.getLessons(group).get(DAY_FROM_ETU.get(index)).stream()
                .filter(it -> it.getAuditoriumReservation().getReservationTime().getWeek().equals(week.toString()))
                .sorted(Comparator.comparingInt(entry -> entry.getAuditoriumReservation().getReservationTime().getStartTime()))
                .forEach(it -> stringBuilder.append(ScheduleUtil.getLessonMessage(it)).append(System.lineSeparator()));
        return stringBuilder.toString();
    }

}
