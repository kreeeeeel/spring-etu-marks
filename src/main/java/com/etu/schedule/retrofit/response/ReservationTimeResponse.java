package com.etu.schedule.retrofit.response;

import lombok.Getter;

@Getter
public class ReservationTimeResponse {
    private Integer startTime;
    private Integer endTime;
    private String weekDay;
    private String week;
}
