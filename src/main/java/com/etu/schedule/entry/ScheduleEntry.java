package com.etu.schedule.entry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleEntry {
    private Integer pair;
    private Integer week;
    private String day;
}
