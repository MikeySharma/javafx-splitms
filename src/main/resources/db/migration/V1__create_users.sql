create table if not exists users (
    id int primary key auto_increment,
    name varchar(200) not null,
    email varchar(320) not null unique,
    password_hash varchar(200) not null,
    created_at timestamp not null default current_timestamp
) engine=InnoDB default charset=utf8mb4;
