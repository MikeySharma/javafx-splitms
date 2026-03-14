package com.splitms.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.splitms.lib.Database;

public class GroupMembersServiceTest {

    private static final String OWNER_EMAIL = "members-owner@example.com";
    private static final String OWNER_NAME = "Members Owner";
    private static final String FRIEND_EMAIL = "members-friend@example.com";
    private static final String FRIEND_NAME = "Members Friend";
    private static final String PASSWORD = "password123";

    private GroupMembersService groupMembersService;
    private GroupsService groupsService;
    private int ownerUserId;
    private int friendUserId;
    private int groupId;

    @Before
    public void setUp() {
        groupMembersService = new GroupMembersService();
        groupsService = new GroupsService();

        cleanupByEmail(FRIEND_EMAIL);
        cleanupByEmail(OWNER_EMAIL);

        ownerUserId = registerAndLogin(OWNER_NAME, OWNER_EMAIL, PASSWORD);
        friendUserId = registerAndLogin(FRIEND_NAME, FRIEND_EMAIL, PASSWORD);

        assertTrue("Owner group should be created", groupsService.createDefaultPersonalGroupForUser(ownerUserId));
        groupId = fetchLatestGroupIdByOwner(ownerUserId);
        assertTrue("Group id should be valid", groupId > 0);
    }

    @Test
    public void testAddMemberAndIsMember() {
        boolean added = groupMembersService.addMember(groupId, friendUserId);
        assertTrue("Member should be added to group", added);
        assertTrue("Member should exist in group", groupMembersService.isMember(groupId, friendUserId));
    }

    @Test
    public void testAddMemberDuplicateReturnsFalse() {
        assertTrue("First add should succeed", groupMembersService.addMember(groupId, friendUserId));
        assertFalse("Duplicate add should be ignored", groupMembersService.addMember(groupId, friendUserId));
    }

    @Test
    public void testRemoveMember() {
        assertTrue("First add should succeed", groupMembersService.addMember(groupId, friendUserId));
        assertTrue("Remove should succeed", groupMembersService.removeMember(groupId, friendUserId));
        assertFalse("Member should no longer exist", groupMembersService.isMember(groupId, friendUserId));
    }

    @Test
    public void testGetMemberUserIdsAndGroupIdsForUser() {
        assertTrue("Add should succeed", groupMembersService.addMember(groupId, friendUserId));

        List<Integer> memberIds = groupMembersService.getMemberUserIds(groupId);
        List<Integer> groupIds = groupMembersService.getGroupIdsForUser(friendUserId);

        assertTrue("Group members should include friend user", memberIds.contains(friendUserId));
        assertTrue("Friend memberships should include group", groupIds.contains(groupId));
    }

    @Test
    public void testInvalidIds() {
        assertFalse("Invalid group id should fail", groupMembersService.addMember(0, friendUserId));
        assertFalse("Invalid user id should fail", groupMembersService.addMember(groupId, 0));
        assertEquals("No members for invalid group", 0, groupMembersService.getMemberUserIds(0).size());
        assertEquals("No groups for invalid user", 0, groupMembersService.getGroupIdsForUser(0).size());
    }

    private static int registerAndLogin(String name, String email, String password) {
        UserService user = new UserService();
        if (!user.register(name, email, password)) {
            throw new RuntimeException("Failed to register test user: " + email);
        }

        int userId = user.login(email, password);
        if (userId <= 0) {
            throw new RuntimeException("Failed to login test user: " + email);
        }
        return userId;
    }

    private static int fetchLatestGroupIdByOwner(int ownerUserId) {
        String sql = "SELECT group_id FROM `group` WHERE user_id = " + ownerUserId
                + " ORDER BY group_id DESC LIMIT 1";

        try (ResultSet rs = Database.executeQuery(sql)) {
            if (!rs.next()) {
                return -1;
            }
            return rs.getInt("group_id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch owner group", e);
        }
    }

    private static void cleanupByEmail(String email) {
        String safeEmail = email.replace("'", "''");
        String userIdSql = "SELECT id FROM users WHERE email = '" + safeEmail + "' LIMIT 1";

        try (ResultSet rs = Database.executeQuery(userIdSql)) {
            if (!rs.next()) {
                return;
            }

            int userId = rs.getInt("id");
            Database.executeUpdate("DELETE FROM group_members WHERE user_id = " + userId);
            Database.executeUpdate("DELETE FROM `group` WHERE user_id = " + userId);
            Database.executeUpdate("DELETE FROM users WHERE id = " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed cleaning test user", e);
        }
    }
}
