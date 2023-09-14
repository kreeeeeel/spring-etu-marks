package com.etu.schedule.retrofit.response;

import lombok.Getter;

import java.util.List;

@Getter
public class GroupResponse {
    private String fullNumber;
    private List<ScheduleResponse> scheduleObjects;
}
