package com.splitms.services;

import com.splitms.repositories.GroupMembershipRepository;
import com.splitms.repositories.GroupRepository;
import com.splitms.repositories.JdbcGroupMembershipRepository;
import com.splitms.repositories.JdbcGroupRepository;
import com.splitms.repositories.JdbcUserRepository;
import com.splitms.repositories.UserRepository;
import com.splitms.services.security.PasswordHasher;
import com.splitms.services.security.Pbkdf2PasswordHasher;

public final class ApplicationServices {

    private static final UserRepository USER_REPOSITORY = new JdbcUserRepository();
    private static final GroupRepository GROUP_REPOSITORY = new JdbcGroupRepository();
    private static final GroupMembershipRepository GROUP_MEMBERSHIP_REPOSITORY = new JdbcGroupMembershipRepository();
    private static final PasswordHasher PASSWORD_HASHER = new Pbkdf2PasswordHasher();

    private static final UserService USER_SERVICE = new UserService(
            USER_REPOSITORY,
            GROUP_REPOSITORY,
            PASSWORD_HASHER);

    private static final GroupsService GROUPS_SERVICE = new GroupsService(GROUP_REPOSITORY);
    private static final GroupMembersService GROUP_MEMBERS_SERVICE = new GroupMembersService(
            GROUP_MEMBERSHIP_REPOSITORY,
            USER_REPOSITORY);

    private ApplicationServices() {
    }

    public static UserService userService() {
        return USER_SERVICE;
    }

    public static GroupsService groupsService() {
        return GROUPS_SERVICE;
    }

    public static GroupMembersService groupMembersService() {
        return GROUP_MEMBERS_SERVICE;
    }
}
