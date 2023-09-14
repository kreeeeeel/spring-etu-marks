package com.etu.schedule.retrofit;

import com.etu.schedule.retrofit.response.GroupResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface EtuApi {

    @GET("/api/schedule/objects/publicated")
    Call<List<GroupResponse>> getSchedule(
            @Query("facultyId") Integer facultyId,
            @Query("courses") Integer courses,
            @Query("withSubjectCode") boolean withSubjectCode,
            @Query("withURL") boolean withURL,
            @Query("studyingType") String studyingType
    );


}
