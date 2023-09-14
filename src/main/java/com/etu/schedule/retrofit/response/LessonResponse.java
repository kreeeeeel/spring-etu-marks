package com.etu.schedule.retrofit.response;

import lombok.Getter;

@Getter
public class LessonResponse {
    private SubjectResponse subject;
    private AuditoriumResponse auditoriumReservation;
    private TeacherResponse teacher;
}
