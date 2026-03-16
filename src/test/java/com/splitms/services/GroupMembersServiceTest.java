package com.splitms.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.splitms.models.GroupMemberView;
import com.splitms.models.GroupModel;
import com.splitms.models.UserAccount;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class GroupMembersServiceTest {

    private static final String OWNER_EMAIL = "members-owner@example.com";
    private static final String OWNER_NAME = "Members Owner";
    private static final String FRIEND_EMAIL = "members-friend@example.com";
    private static final String FRIEND_NAME = "Members Friend";
    private static final String PASSWORD = "password123";

    private GroupMembersService groupMembersService;
    private GroupsService groupsService;
    private UserService userService;
    private int ownerUserId;
    private int friendUserId;
    private int groupId;

    @Before
    public void setUp() {
        groupMembersService = new GroupMembersService();
        groupsService = new GroupsService();
        userService = new UserService();

        UserService.deleteByEmail(FRIEND_EMAIL);
        UserService.deleteByEmail(OWNER_EMAIL);

        ServiceResult<UserAccount> ownerRegister = userService.register(OWNER_NAME, OWNER_EMAIL, PASSWORD);
        ServiceResult<UserAccount> friendRegister = userService.register(FRIEND_NAME, FRIEND_EMAIL, PASSWORD);
        assertTrue("Owner registration should succeed", ownerRegister.success());
        assertTrue("Friend registration should succeed", friendRegister.success());

        ownerUserId = ownerRegister.data().userId();
        friendUserId = friendRegister.data().userId();

        ServiceResult<List<GroupModel>> ownerGroups = groupsService.listGroupsForUser(ownerUserId, "");
        assertTrue("Owner groups should load", ownerGroups.success());
        groupId = ownerGroups.data().get(0).groupId();
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
        ServiceResult<Void> removeResult = groupMembersService.removeMember(groupId, friendUserId);
        assertTrue("Remove should succeed", removeResult.success());
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

    @Test
    public void testListMembersContract() {
        assertTrue("Add should succeed", groupMembersService.addMember(groupId, friendUserId));
        ServiceResult<List<GroupMemberView>> membersResult = groupMembersService.listMembers(groupId);
        assertTrue("List members should succeed", membersResult.success());
        assertFalse("Members should not be empty", membersResult.data().isEmpty());
    }
}
