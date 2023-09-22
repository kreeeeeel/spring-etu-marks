package com.etu.schedule.repository;

import com.etu.schedule.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByTelegramId(Long telegramId);

    @Query("""
        select u from UserEntity u
        where u.groupEtu in (select s.groupEtu from ScheduleEntity s where s.week = :week and s.pair = :pair and s.day = :day)
        and u.email is not null
        and u.password is not null
        and u.isNote = true
    """)
    List<UserEntity> findUserForNote(
            @Param("week") Integer week,
            @Param("day") Integer day,
            @Param("pair") Integer pair
    );


    @Query("select u.groupSchedule from UserEntity u where u.telegramId = :telegram")
    String getGroupByUser(@Param("telegram") Long telegram);

    boolean existsByTelegramIdAndGroupScheduleIsNotNull(Long telegram);
}
