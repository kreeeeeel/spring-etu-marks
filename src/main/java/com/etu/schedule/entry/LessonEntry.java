package com.etu.schedule.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LessonEntry {
    private String shortName;
    private String name;
    private Integer pair;
    private String teacher;
    private String type;
    private String auditorium;
}
