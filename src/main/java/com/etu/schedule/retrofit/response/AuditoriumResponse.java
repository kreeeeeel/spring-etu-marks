package com.etu.schedule.retrofit.response;

import lombok.Getter;

@Getter
public class AuditoriumResponse {
    private String auditoriumNumber;
    private ReservationTimeResponse reservationTime;
}
