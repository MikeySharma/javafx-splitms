package com.splitms.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.splitms.models.GroupModel;
import com.splitms.models.UserAccount;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class GroupsServiceTest {

    private static final String OWNER_EMAIL = "groups-owner@example.com";
    private static final String OWNER_NAME = "Groups Owner";
    private static final String OWNER_PASSWORD = "password123";

    private GroupsService groupsService;
    private UserService userService;
    private int ownerUserId;

    @Before
    public void setUp() {
        groupsService = new GroupsService();
        userService = new UserService();

        UserService.deleteByEmail(OWNER_EMAIL);

        ServiceResult<UserAccount> registerResult = userService.register(OWNER_NAME, OWNER_EMAIL, OWNER_PASSWORD);
        assertTrue("Owner registration should succeed", registerResult.success());
        assertNotNull("Owner account should be returned", registerResult.data());
        ownerUserId = registerResult.data().userId();
    }

    @Test
    public void testListGroupsForUser() {
        ServiceResult<List<GroupModel>> groupsResult = groupsService.listGroupsForUser(ownerUserId, "");
        assertTrue("Group list should load", groupsResult.success());
        assertNotNull("Group list data should not be null", groupsResult.data());
        assertFalse("At least default personal group should exist", groupsResult.data().isEmpty());
    }

    @Test
    public void testCreateUpdateDeleteGroup() {
        ServiceResult<GroupModel> createResult = groupsService.createGroup(ownerUserId, "Trip Group", "Goa travel split");
        assertTrue("Group create should succeed", createResult.success());
        assertNotNull("Created group should be returned", createResult.data());
        int groupId = createResult.data().groupId();

        ServiceResult<GroupModel> updateResult = groupsService.updateGroup(
                groupId,
                ownerUserId,
                "Trip Group Updated",
                "Updated description");

        assertTrue("Group update should succeed", updateResult.success());
        assertNotNull("Updated group should be returned", updateResult.data());
        assertEquals("Updated name should match", "Trip Group Updated", updateResult.data().groupName());

        ServiceResult<Void> deleteResult = groupsService.deleteGroup(groupId, ownerUserId);
        assertTrue("Group delete should succeed", deleteResult.success());
    }

    @Test
    public void testCreateDefaultPersonalGroupForUserWithInvalidUser() {
        boolean created = GroupsService.createDefaultPersonalGroupForUser(0);
        assertFalse("Invalid user id should not create a group", created);
    }
}
