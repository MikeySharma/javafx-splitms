package com.splitms.services;

import com.splitms.interfaces.GroupService;
import com.splitms.models.GroupModel;
import com.splitms.repositories.GroupRepository;
import com.splitms.repositories.JdbcGroupRepository;
import com.splitms.utils.Normalize;
import java.util.List;

public class GroupsService implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMembersService groupMembersService;

    public GroupsService() {
        this(new JdbcGroupRepository(), null);
    }

    public GroupsService(GroupRepository groupRepository) {
        this(groupRepository, null);
    }

    public GroupsService(GroupRepository groupRepository, GroupMembersService groupMembersService) {
        this.groupRepository = groupRepository;
        this.groupMembersService = groupMembersService;
    }

    @Override
    public ServiceResult<List<GroupModel>> listGroupsForUser(int userId, String searchQuery) {
        if (userId <= 0) {
            return ServiceResult.fail("Invalid user id.");
        }

        List<GroupModel> groups = groupRepository.listByOwner(userId, Normalize.normalizeText(searchQuery));
        return ServiceResult.ok("Groups loaded.", groups);
    }

    @Override
    public ServiceResult<GroupModel> createGroup(int userId, String name, String description) {
        String normalizedName = Normalize.normalizeText(name);
        String normalizedDescription = Normalize.normalizeText(description);

        if (userId <= 0 || normalizedName.isBlank()) {
            return ServiceResult.fail("Group name is required.");
        }

        String safeDescription = normalizedDescription.isBlank()
                ? "No description"
                : normalizedDescription;

        int groupId = groupRepository.create(userId, normalizedName, safeDescription, false);
        if (groupId <= 0) {
            return ServiceResult.fail("Could not create group.");
        }

        // Automatically add creator as a group member
        if (groupMembersService != null) {
            groupMembersService.addMember(groupId, userId);
        }

        return groupRepository.findByIdAndOwner(groupId, userId)
                .map(group -> ServiceResult.ok("Group created.", group))
                .orElseGet(() -> ServiceResult.fail("Group created but could not be loaded."));
    }

    @Override
    public ServiceResult<GroupModel> updateGroup(int groupId, int ownerUserId, String name, String description) {
        String normalizedName = Normalize.normalizeText(name);
        String normalizedDescription = Normalize.normalizeText(description);

        if (groupId <= 0 || ownerUserId <= 0 || normalizedName.isBlank()) {
            return ServiceResult.fail("Group name is required.");
        }

        String safeDescription = normalizedDescription.isBlank()
                ? "No description"
                : normalizedDescription;

        boolean updated = groupRepository.update(groupId, ownerUserId, normalizedName, safeDescription);
        if (!updated) {
            return ServiceResult.fail("Group update failed.");
        }

        return groupRepository.findByIdAndOwner(groupId, ownerUserId)
                .map(group -> ServiceResult.ok("Group updated.", group))
                .orElseGet(() -> ServiceResult.fail("Group updated but could not be reloaded."));
    }

    @Override
    public ServiceResult<Void> deleteGroup(int groupId, int ownerUserId) {
        if (groupId <= 0 || ownerUserId <= 0) {
            return ServiceResult.fail("Invalid group id.");
        }

        // Check if the group is a personal default group
        var groupOption = groupRepository.findByIdAndOwner(groupId, ownerUserId);
        if (groupOption.isPresent() && groupOption.get().personalDefault()) {
            return ServiceResult.fail("Personal default groups cannot be deleted. You can only edit them.");
        }

        boolean deleted = groupRepository.delete(groupId, ownerUserId);
        if (!deleted) {
            return ServiceResult.fail("Group delete failed.");
        }

        return ServiceResult.ok("Group deleted.", null);
    }

    public static boolean createDefaultPersonalGroupForUser(int userId) {
        if (userId <= 0) {
            return false;
        }

        int created = new JdbcGroupRepository().create(
                userId,
                "Personal Group",
                "This is your personal default group.",
                true);
        return created > 0;
    }
}
