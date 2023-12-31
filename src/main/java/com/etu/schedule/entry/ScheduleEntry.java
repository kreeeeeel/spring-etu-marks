package com.etu.schedule.entry;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScheduleEntry {
    private String group;
    private Integer week;
    private List<DayEntry> entry;
}
