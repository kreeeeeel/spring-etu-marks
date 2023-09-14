package com.etu.schedule.entry;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PairEntry {
    private String group;
    private String shortTitle;
    private String lessonType;
    private String auditorium;
    private String teacher;
    private Integer week;
    private Integer pair;
    private String day;
}
