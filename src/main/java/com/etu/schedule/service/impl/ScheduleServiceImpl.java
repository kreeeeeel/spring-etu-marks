package com.etu.schedule.service.impl;

import com.etu.schedule.entity.ScheduleEntity;
import com.etu.schedule.entry.*;
import com.etu.schedule.repository.ScheduleRepository;
import com.etu.schedule.retrofit.EtuApi;
import com.etu.schedule.retrofit.response.GroupResponse;
import com.etu.schedule.retrofit.response.ScheduleResponse;
import com.etu.schedule.service.ScheduleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final List<String> DAY_FROM_ETU = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT");
    private final Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://digital.etu.ru")
            .build();

    private final ScheduleRepository scheduleRepository;

    private int currentPair, currentDay, currentWeek;

    @Override
    public int getCurrentPair(){
        return currentPair;
    }

    @Override
    public int getCurrentDay(){
        return currentDay;
    }

    @Override
    public int getCurrentWeek(){
        return currentWeek;
    }

    @Override
    public boolean isExistGroup(String group){
        return !scheduleRepository.existsByGroupEtu(group);
    }

    @Override
    public ScheduleEntry getLessonWeek(String group, boolean next) {

        int week = next ? (currentWeek + 1 > 2 ? 1 : 2) : currentWeek;
        return ScheduleEntry.builder()
                .group(group)
                .week(week)
                .entry(scheduleRepository.findByGroupEtuAndWeek(group, week).stream()
                        .collect(Collectors.groupingBy(ScheduleEntity::getDay))
                        .entrySet().stream()
                        .map(it -> DayEntry.builder()
                                .day(it.getKey())
                                .lesson(it.getValue().stream()
                                        .map(this::entityToLessonEntry)
                                        .sorted(Comparator.comparingInt(LessonEntry::getPair))
                                        .toList()
                                )
                                .build())
                        .sorted(Comparator.comparingInt(DayEntry::getDay))
                        .toList()
                )
                .build();
    }

    @Override
    public LessonDayEntry getLessonDay(String group, boolean next) {

        int day = next ? ( currentDay + 1 > 5 ? 0 : currentDay + 1) : currentDay;
        int week = next ? (currentDay + 1 > 5 ? (currentWeek + 1 > 2) ? 1 : 2 : currentWeek) : currentWeek;

        return LessonDayEntry.builder()
                .day(day)
                .week(week)
                .lesson(scheduleRepository.findByGroupEtuAndWeekAndDay(group, week, day).stream()
                        .map(this::entityToLessonEntry)
                        .sorted(Comparator.comparingInt(LessonEntry::getPair))
                        .toList())
                .build();
    }

    @Override
    public LessonEntry getLessonCurrent(String group, boolean next) {

        LocalTime localTime = LocalTime.now();
        if (currentPair == -1 && (!next || localTime.isAfter(LocalTime.of(19, 0))
                && localTime.isBefore(LocalTime.of(23, 59)))){
            return null;
        }

        ScheduleEntity scheduleEntity = next ? scheduleRepository.findByNextPair(group, currentWeek, currentDay, currentPair) :
                scheduleRepository.findByCurrentPair(group, currentWeek, currentDay, currentPair);

        if (scheduleEntity == null){
            return null;
        }
        return entityToLessonEntry(scheduleEntity);
    }

    @PostConstruct
    public void initialize() {

        if (scheduleRepository.count() == 0) {
            log.info("Parsing schedules for groups.");
            List<ScheduleEntity> entities = new ArrayList<>();
            IntStream.rangeClosed(1, 7)
                    .forEach(faculty ->
                            IntStream.rangeClosed(1, 6)
                                    .forEach(course -> entities.addAll(getSchedule(faculty, course)))

                    );
            scheduleRepository.saveAll(entities);
            log.info(entities.size() + " number of lessons received");
        }

    }

    @SneakyThrows
    private List<ScheduleEntity> getSchedule(int faculty, int course){
        return Objects.requireNonNull(
                retrofit.create(EtuApi.class)
                        .getSchedule(faculty, course, true, true, "оч")
                        .execute()
                        .body())
                .stream()
                .flatMap(it -> it.getScheduleObjects()
                        .stream()
                        .map(th -> responseToEntity(it, th)))
                .toList();
    }

    @PostConstruct
    @Scheduled(cron = "0 0 8 ? * MON-SAT")
    @Scheduled(cron = "0 50 9 ? * MON-SAT")
    @Scheduled(cron = "0 40 11 ? * MON-SAT")
    @Scheduled(cron = "0 40 13 ? * MON-SAT")
    @Scheduled(cron = "0 30 15 ? * MON-SAT")
    @Scheduled(cron = "0 20 17 ? * MON-SAT")
    @Scheduled(cron = "0 0 19 ? * MON-SAT")
    public void changeCurrentPair() {
        LocalTime currentTime = LocalTime.now();
        List<LocalTime[]> timeRanges = Arrays.asList(
                new LocalTime[] {LocalTime.of(7, 50), LocalTime.of(9, 30)},
                new LocalTime[] {LocalTime.of(9, 30), LocalTime.of(11, 20)},
                new LocalTime[] {LocalTime.of(11, 20), LocalTime.of(13, 10)},
                new LocalTime[] {LocalTime.of(13, 10), LocalTime.of(15, 10)},
                new LocalTime[] {LocalTime.of(15, 10), LocalTime.of(17, 0)},
                new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(18, 50)}
        );

        currentPair = IntStream.range(0, timeRanges.size())
                .filter(it -> currentTime.isAfter(timeRanges.get(it)[0]) && currentTime.isBefore(timeRanges.get(it)[1]))
                .findFirst()
                .orElse(-1);

        log.info(currentPair != -1 ? "Current pair number is " + (currentPair + 1) : "Pairs the end.");
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 ? * *")
    public void changeCurrentDayWithWeek(){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        currentWeek = calendar.get(Calendar.WEEK_OF_MONTH) % 2 != 0 ? 1 : 2;
        log.info("Changed day for " + currentDay + " and week " + currentWeek);
    }

    private LessonEntry entityToLessonEntry(ScheduleEntity scheduleEntity) {
        return LessonEntry.builder()
                .name(scheduleEntity.getName())
                .pair(scheduleEntity.getPair())
                .teacher(scheduleEntity.getTeacherShortName())
                .type(scheduleEntity.getType())
                .auditorium(scheduleEntity.getAuditorium())
                .shortName(scheduleEntity.getShortName())
                .build();
    }

    private ScheduleEntity responseToEntity(GroupResponse groupResponse, ScheduleResponse scheduleResponse) {
        return ScheduleEntity.builder()
                .groupEtu(groupResponse.getFullNumber())
                .shortName(scheduleResponse.getLesson().getSubject().getShortTitle())
                .name(scheduleResponse.getLesson().getSubject().getTitle())
                .type(scheduleResponse.getLesson().getSubject().getSubjectType())
                .teacherShortName(scheduleResponse.getLesson().getTeacher() != null ? scheduleResponse.getLesson().getTeacher().getInitials() : null)
                .teacherName(scheduleResponse.getLesson().getTeacher() != null ? String.format("%s %s %s",
                        scheduleResponse.getLesson().getTeacher().getSurname(),
                        scheduleResponse.getLesson().getTeacher().getName(),
                        scheduleResponse.getLesson().getTeacher().getMidname()) : null)
                .auditorium(scheduleResponse.getLesson().getAuditoriumReservation().getAuditoriumNumber())
                .pair(scheduleResponse.getLesson().getAuditoriumReservation().getReservationTime().getStartTime() % 100)
                .week(Integer.valueOf(scheduleResponse.getLesson().getAuditoriumReservation().getReservationTime().getWeek()))
                .day(DAY_FROM_ETU.indexOf(scheduleResponse.getLesson().getAuditoriumReservation().getReservationTime().getWeekDay()))
                .build();
    }

}
