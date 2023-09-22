package com.etu.schedule.telegram.util;

import com.etu.schedule.entry.LessonEntry;
import lombok.experimental.UtilityClass;

import static com.etu.schedule.service.impl.TelegramServiceImpl.TIME;

@UtilityClass
public class ScheduleUtil {

    public String getLessonMessage(LessonEntry lessonEntry) {
        String teacher = lessonEntry.getTeacher() != null ? System.lineSeparator() + String.format(" - %s", lessonEntry.getTeacher()) : "";
        String auditorium = lessonEntry.getAuditorium() != null ? "ауд." + lessonEntry.getAuditorium() : "";

        return String.format("%s - %s (%s) %s%s",
                TIME.get(lessonEntry.getPair()),
                lessonEntry.getShortName(),
                lessonEntry.getType(),
                auditorium,
                teacher
        );
    }

}
