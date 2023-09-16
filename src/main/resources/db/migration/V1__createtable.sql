create table groups
(
    id bigserial primary key,
    group_etu varchar(255),
    is_notify boolean,
    telegram_id bigint
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