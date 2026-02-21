create table if not exists users (
    id bigserial primary key,
    name varchar(200) not null,
    email varchar(320) not null unique,
    password_hash varchar(200) not null,
    created_at timestamptz not null default now()
);
