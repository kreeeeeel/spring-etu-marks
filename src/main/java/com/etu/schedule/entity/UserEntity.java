package com.etu.schedule.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long telegramId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String groupEtu;

    @Column
    private String groupSchedule;

    @Column
    @Builder.Default
    private boolean isNote = false;

}
