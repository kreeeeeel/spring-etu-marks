package com.etu.schedule.telegram.util;

import com.etu.schedule.retrofit.response.LessonResponse;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import static com.etu.schedule.ScheduleApplication.TIME;

@UtilityClass
public class ScheduleUtil {

    public String getLessonMessage(LessonResponse lessonResponse) {
        String teacher = lessonResponse.getTeacher() != null ? System.lineSeparator() + String.format(" - %s", lessonResponse.getTeacher().getInitials()) : "";
        String auditorium = lessonResponse.getAuditoriumReservation().getAuditoriumNumber() != null ? "ауд." + lessonResponse.getAuditoriumReservation().getAuditoriumNumber() : "";

        return String.format("%s - %s (%s) %s%s",
                TIME.get(lessonResponse.getAuditoriumReservation().getReservationTime().getStartTime() % 100),
                lessonResponse.getSubject().getShortTitle(),
                lessonResponse.getSubject().getSubjectType(),
                auditorium,
                teacher
        );
    }

}
