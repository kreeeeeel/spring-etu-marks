package com.etu.schedule.entry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GroupEntry {
    private String group;
    private String shortTitle;
    private String lessonType;
    private String auditorium;
    private String teacher;
}
