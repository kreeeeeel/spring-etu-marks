package com.etu.schedule.entry;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LessonDayEntry {
    private Integer day;
    private Integer week;
    private List<LessonEntry> lesson;
}
