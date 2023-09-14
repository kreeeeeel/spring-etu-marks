package com.etu.schedule.service;

import com.etu.schedule.entry.GroupEntry;
import com.etu.schedule.entry.PairEntry;
import com.etu.schedule.retrofit.response.LessonResponse;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    Integer getPair();
    Integer getWeek();
    Integer getCountGroup();
    List<GroupEntry> getLesson();
    boolean isGroup(String group);
    List<PairEntry> getLessonNow();
    List<PairEntry> getLessonNext();
    Map<String, List<LessonResponse>> getLessons(String group);
}
