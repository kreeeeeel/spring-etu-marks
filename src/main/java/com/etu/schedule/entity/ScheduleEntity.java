package com.etu.schedule.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer pair;

    @Column
    private Integer week;

    @Column
    private Integer day;

    @Column
    private String groupEtu;

    @Column
    private String type;

    @Column
    private String name;

    @Column
    private String shortName;

    @Column
    private String teacherName;

    @Column
    private String teacherShortName;

    @Column
    private String auditorium;

}