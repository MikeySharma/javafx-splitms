create table if not exists `group` (
    group_id int primary key auto_increment,
    user_id int not null,
    group_name varchar(200) not null,
    description varchar(500) not null,
    is_personal_default boolean not null default false,
    created_at timestamp not null default current_timestamp,
    constraint fk_group_user
        foreign key (user_id) references users(id)
        on delete cascade
) engine=InnoDB default charset=utf8mb4;
