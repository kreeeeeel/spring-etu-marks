create table groups
(
    id bigserial primary key,
    group_etu varchar(255),
    is_notify boolean,
    telegram_id bigint
);

create table schedule
(
    id bigserial primary key,
    auditorium varchar(255),
    day integer,
    group_etu varchar(255),
    name varchar(255),
    pair integer,
    short_name varchar(255),
    teacher_name varchar(255),
    teacher_short_name varchar(255),
    type varchar(255),
    week integer
);

create table users
(
    id bigserial primary key,
    email varchar(255),
    group_etu varchar(255),
    group_schedule varchar(255),
    is_note boolean,
    name varchar(255),
    password varchar(255),
    telegram_id bigint
);