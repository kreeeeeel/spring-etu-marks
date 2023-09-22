package com.etu.schedule.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DayEntry {
    private Integer day;
    private List<LessonEntry> lesson;
}
