package com.splitms.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.splitms.lib.Database;

public class GroupsServiceTest {

    private static final String OWNER_EMAIL = "groups-owner@example.com";
    private static final String OWNER_NAME = "Groups Owner";
    private static final String OWNER_PASSWORD = "password123";

    private GroupsService groupsService;
    private int ownerUserId;

    @Before
    public void setUp() {
        groupsService = new GroupsService();
        cleanupByEmail(OWNER_EMAIL);

        UserService owner = new UserService();
        assertTrue("Owner registration should succeed", owner.register(OWNER_NAME, OWNER_EMAIL, OWNER_PASSWORD));

        ownerUserId = owner.login(OWNER_EMAIL, OWNER_PASSWORD);
        assertTrue("Owner user id should be valid", ownerUserId > 0);
    }

    @Test
    public void testCreateDefaultPersonalGroupForUserWithValidUser() {
        boolean created = groupsService.createDefaultPersonalGroupForUser(ownerUserId);
        assertTrue("Default personal group should be created", created);
        assertTrue("Group should exist for owner", hasDefaultGroup(ownerUserId));
    }

    @Test
    public void testCreateDefaultPersonalGroupForUserWithInvalidUser() {
        boolean created = groupsService.createDefaultPersonalGroupForUser(0);
        assertFalse("Invalid user id should not create a group", created);
    }

    private static boolean hasDefaultGroup(int userId) {
        String sql = "SELECT 1 FROM `group` WHERE user_id = " + userId
                + " AND group_name = 'Personal Group'"
                + " AND is_personal_default = true LIMIT 1";

        try (ResultSet rs = Database.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed checking default group", e);
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
