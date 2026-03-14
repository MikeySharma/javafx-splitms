create table if not exists group_members (
    id int primary key auto_increment,
    group_id int not null,
    user_id int not null,
    added_at timestamp not null default current_timestamp,
    constraint uq_group_member unique (group_id, user_id),
    constraint fk_gm_group foreign key (group_id) references `group`(group_id) on delete cascade,
    constraint fk_gm_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;
