package com.etu.schedule.repository;

import com.etu.schedule.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("select g from GroupEntity g where g.isNotify = true and g.groupEtu != null")
    List<GroupEntity> getGroupForNotify();

    @Query("select g.groupEtu from GroupEntity g where g.telegramId = :telegram")
    String getGroupByChat(@Param("telegram") Long telegram);

    Optional<GroupEntity> findByTelegramId(Long telegram);

    boolean existsByTelegramId(Long telegram);

    boolean existsByTelegramIdAndGroupEtuIsNotNull(Long telegram);

}
