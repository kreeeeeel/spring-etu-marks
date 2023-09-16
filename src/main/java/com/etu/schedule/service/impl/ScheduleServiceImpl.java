package com.etu.schedule.service.impl;

import com.etu.schedule.entry.GroupEntry;
import com.etu.schedule.entry.PairEntry;
import com.etu.schedule.entry.ScheduleEntry;
import com.etu.schedule.exception.ParseScheduleException;
import com.etu.schedule.retrofit.EtuApi;
import com.etu.schedule.retrofit.response.GroupResponse;
import com.etu.schedule.retrofit.response.LessonResponse;
import com.etu.schedule.retrofit.response.ScheduleResponse;
import com.etu.schedule.service.ScheduleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.IntStream;

import static com.etu.schedule.ScheduleApplication.DAY_FROM_ETU;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private Integer pair = -1;

    private final Map<Integer, List<PairEntry>> map = new HashMap<>();
    private final Map<ScheduleEntry, List<GroupEntry>> pairsByTimeAndDay = new HashMap<>();
    private final Map<String, Map<String, List<LessonResponse>>> groupsByGroupAndDay = new HashMap<>();

    @Override
    public List<GroupEntry> getLesson() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        String day = DAY_FROM_ETU.get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        return pairsByTimeAndDay.entrySet().stream()
                .filter(it -> it.getKey().getWeek().equals(getWeek()) && it.getKey().getDay().equals(day) && it.getKey().getPair().equals(pair))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>())
                .stream().toList();
    }

    @Override
    public Map<String, List<LessonResponse>> getLessons(String group) {
        return groupsByGroupAndDay.get(group);
    }

    @Override
    public Integer getPair() {
        return pair;
    }

    @Override
    public Integer getWeek() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        return calendar.get(Calendar.WEEK_OF_MONTH) % 2 != 0 ? 1 : 2;
    }

    @Override
    public Integer getCountGroup() {
        return groupsByGroupAndDay.size();
    }

    @Override
    public boolean isGroup(String group) {
        return groupsByGroupAndDay.containsKey(group);
    }

    @Override
    public List<PairEntry> getLessonNow() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        String day = DAY_FROM_ETU.get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        List<PairEntry> pairEntries = map.get(pair);
        return pairEntries != null ? pairEntries.stream()
                .filter(it -> it.getWeek().equals(getWeek()) && it.getDay().equals(day))
                .toList() : null;
    }

    @Override
    public List<PairEntry> getLessonNext() {
        if (pair == -1){
            return null;
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        String day = DAY_FROM_ETU.get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        return map.entrySet().stream()
                .filter(it -> it.getKey() > pair)
                .flatMap(it -> it.getValue()
                        .stream()
                        .filter(value -> value.getWeek().equals(getWeek()) && value.getDay().equals(day))
                )
                .toList();
    }

    @PostConstruct
    @Scheduled(cron = "0 0 8 ? * MON-SAT")
    @Scheduled(cron = "0 50 9 ? * MON-SAT")
    @Scheduled(cron = "0 40 11 ? * MON-SAT")
    @Scheduled(cron = "0 40 13 ? * MON-SAT")
    @Scheduled(cron = "0 30 15 ? * MON-SAT")
    @Scheduled(cron = "0 20 17 ? * MON-SAT")
    public void changeNumberPair() {
        LocalTime currentTime = LocalTime.now();
        List<LocalTime[]> timeRanges = Arrays.asList(
                new LocalTime[] {LocalTime.of(7, 50), LocalTime.of(9, 30)},
                new LocalTime[] {LocalTime.of(9, 40), LocalTime.of(11, 20)},
                new LocalTime[] {LocalTime.of(11, 30), LocalTime.of(13, 10)},
                new LocalTime[] {LocalTime.of(13, 30), LocalTime.of(15, 10)},
                new LocalTime[] {LocalTime.of(15, 20), LocalTime.of(17, 0)},
                new LocalTime[] {LocalTime.of(17, 10), LocalTime.of(18, 50)}
        );

        pair = IntStream.range(0, timeRanges.size())
                .filter(it -> currentTime.isAfter(timeRanges.get(it)[0]) && currentTime.isBefore(timeRanges.get(it)[1]))
                .findFirst()
                .orElse(-1);

        log.info(pair != -1 ? "Current pair number is " + (pair + 1) : "Pairs the end.");
    }

    @SneakyThrows
    @PostConstruct
    public void initializeSchedule() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://digital.etu.ru")
                .build();

        EtuApi etuApi = retrofit.create(EtuApi.class);

        log.info("Running queries to get a schedule.");
        IntStream.rangeClosed(1, 7).forEach(faculty ->
                IntStream.rangeClosed(1, 6).forEach(course ->
                        getSchedule(etuApi.getSchedule(faculty, course, true, true, "оч"))
                )
        );
        log.info("Received schedules for " + groupsByGroupAndDay.size() + " groups.");
    }

    private void getSchedule(Call<List<GroupResponse>> response){
        try {
            List<GroupResponse> body = response.execute().body();
            if (body == null){
                throw new ParseScheduleException();
            }

            body.forEach(groupResponse ->
                    groupResponse.getScheduleObjects()
                            .forEach(lessonResponse -> inputSchedule(lessonResponse, groupResponse))
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private void inputSchedule(ScheduleResponse lessonResponse, GroupResponse groupResponse){
        String subjectTitle = lessonResponse.getLesson().getSubject().getShortTitle();
        String lessonType = lessonResponse.getLesson().getSubject().getSubjectType();
        String auditoriumNumber = lessonResponse.getLesson().getAuditoriumReservation().getAuditoriumNumber();
        String teacher = lessonResponse.getLesson().getTeacher() != null ? lessonResponse.getLesson().getTeacher().getInitials() : null;

        Integer startTime = lessonResponse.getLesson().getAuditoriumReservation().getReservationTime().getStartTime() % 100;
        String weekDay = lessonResponse.getLesson().getAuditoriumReservation().getReservationTime().getWeekDay();
        Integer week = Integer.parseInt(lessonResponse.getLesson().getAuditoriumReservation().getReservationTime().getWeek());

        Map<String, List<LessonResponse>> daySchedule = groupsByGroupAndDay.getOrDefault(groupResponse.getFullNumber(), new HashMap<>());
        List<LessonResponse> lessonsForDay = daySchedule.getOrDefault(weekDay, new ArrayList<>());
        lessonsForDay.add(lessonResponse.getLesson());
        daySchedule.put(weekDay, lessonsForDay);
        groupsByGroupAndDay.put(groupResponse.getFullNumber(), daySchedule);

        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .pair(startTime)
                .week(week)
                .day(weekDay)
                .build();

        List<GroupEntry> groupEntries = pairsByTimeAndDay.getOrDefault(scheduleEntry, new ArrayList<>());

        GroupEntry groupEntry = GroupEntry.builder()
                .group(groupResponse.getFullNumber())
                .auditorium(auditoriumNumber)
                .lessonType(lessonType)
                .shortTitle(subjectTitle)
                .teacher(teacher)
                .build();

        groupEntries.add(groupEntry);
        pairsByTimeAndDay.put(scheduleEntry, groupEntries);

        List<PairEntry> orDefault = map.getOrDefault(startTime, new ArrayList<>());
        orDefault.add(PairEntry.builder()
                        .auditorium(auditoriumNumber)
                        .day(weekDay)
                        .group(groupResponse.getFullNumber())
                        .lessonType(lessonType)
                        .shortTitle(subjectTitle)
                        .teacher(teacher)
                        .pair(startTime)
                        .week(week)
                .build());
        map.put(startTime, orDefault);
    }

}