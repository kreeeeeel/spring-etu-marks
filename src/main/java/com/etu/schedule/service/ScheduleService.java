package com.etu.schedule.service;

import com.etu.schedule.entry.LessonDayEntry;
import com.etu.schedule.entry.LessonEntry;
import com.etu.schedule.entry.ScheduleEntry;

public interface ScheduleService {
    int getCurrentPair();
    int getCurrentDay();
    int getCurrentWeek();
    boolean isExistGroup(String group);
    ScheduleEntry getLessonWeek(String group, boolean next);
    LessonDayEntry getLessonDay(String group, boolean next);
    LessonEntry getLessonCurrent(String group, boolean next);
}
