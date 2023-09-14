package com.etu.schedule.repository;

import com.etu.schedule.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByTelegramId(Long telegramId);

    @Query("select u from UserEntity u where u.groupEtu in :groups and u.email != null and u.password != null and u.isNote = true")
    List<UserEntity> findUserForNote(@Param("groups") List<String> groups);

    @Query("select u.groupSchedule from UserEntity u where u.telegramId = :telegram")
    String getGroupByUser(@Param("telegram") Long telegram);

    boolean existsByTelegramIdAndGroupScheduleIsNotNull(Long telegram);
}
