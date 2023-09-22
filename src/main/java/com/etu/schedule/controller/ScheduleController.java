package com.etu.schedule.controller;

import com.etu.schedule.entry.LessonDayEntry;
import com.etu.schedule.entry.LessonEntry;
import com.etu.schedule.entry.ScheduleEntry;
import com.etu.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class ScheduleController {

    private final ScheduleService scheduleService2;

    @GetMapping("/week")
    public ScheduleEntry getSchedule(
            @RequestParam String group,
            @RequestParam(required = false) boolean next
    ) {
        return scheduleService2.getLessonWeek(group, next);
    }

    @GetMapping("/day")
    public LessonDayEntry getDayLesson(
            @RequestParam String group,
            @RequestParam(required = false) boolean next
    ) {
        return scheduleService2.getLessonDay(group, next);
    }

    @GetMapping("/lesson")
    public LessonEntry getLesson(
            @RequestParam String group,
            @RequestParam(required = false) boolean next
    ) {
        return scheduleService2.getLessonCurrent(group, next);
    }

}
