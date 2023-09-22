package com.etu.schedule.repository;

import com.etu.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByGroupEtuAndWeek(String group, Integer week);
    List<ScheduleEntity> findByGroupEtuAndWeekAndDay(String group, Integer week, Integer day);

    boolean existsByGroupEtu(String group);

    @Query("""
    select s from ScheduleEntity s
    where s.groupEtu = :group and s.week = :week and s.day = :day and s.pair = :pair
    """)
    ScheduleEntity findByCurrentPair(
            @Param("group") String group,
            @Param("week") Integer week,
            @Param("day") Integer day,
            @Param("pair") Integer pair
    );

    @Query("""
    select s from ScheduleEntity s
    where s.groupEtu = :group and s.week = :week and s.day = :day and s.pair = (
        select min(s2.pair) from ScheduleEntity s2
        where s2.groupEtu = :group and s2.week = :week and s2.day = :day and s2.pair > :pair
    )
    """)
    ScheduleEntity findByNextPair(
            @Param("group") String group,
            @Param("week") Integer week,
            @Param("day") Integer day,
            @Param("pair") Integer pair
    );
}
