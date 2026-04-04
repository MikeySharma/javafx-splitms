package com.splitms.services;

import com.splitms.interfaces.GroupMembershipService;
import com.splitms.lib.Database;
import com.splitms.models.GroupMemberView;
import com.splitms.models.UserAccount;
import com.splitms.repositories.GroupMembershipRepository;
import com.splitms.repositories.JdbcGroupMembershipRepository;
import com.splitms.repositories.JdbcUserRepository;
import com.splitms.repositories.UserRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.splitms.utils.Normalize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupMembersService implements GroupMembershipService {

    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public GroupMembersService() {
        this(new JdbcGroupMembershipRepository(), new JdbcUserRepository());
    }

    public GroupMembersService(
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ServiceResult<List<GroupMemberView>> listMembers(int groupId) {
        if (groupId <= 0) {
            return ServiceResult.fail("Invalid group id.");
        }

        List<GroupMemberView> members = membershipRepository.listMembers(groupId);
        return ServiceResult.ok("Members loaded.", members);
    }

    @Override
    public ServiceResult<Void> addMemberByEmail(int groupId, String email) {
        if (groupId <= 0) {
            return ServiceResult.fail("Invalid group id.");
        }

        String normalizedEmail = Normalize.normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            return ServiceResult.fail("Email is required.");
        }

        UserAccount user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            return ServiceResult.fail("No user found with that email.");
        }

        boolean added = membershipRepository.addMember(groupId, user.userId());
        if (!added) {
            return ServiceResult.fail("Member already exists in this group.");
        }

        return ServiceResult.ok("Member added.", null);
    }

    @Override
    public ServiceResult<Void> removeMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return ServiceResult.fail("Invalid member selection.");
        }

        boolean removed = membershipRepository.removeMember(groupId, userId);
        if (!removed) {
            return ServiceResult.fail("Could not remove member.");
        }

        return ServiceResult.ok("Member removed.", null);
    }

    public boolean addMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return false;
        }

        return membershipRepository.addMember(groupId, userId);
    }
    
    public boolean isMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return false;
        }

        return membershipRepository.isMember(groupId, userId);
    }

    public List<Integer> getMemberUserIds(int groupId) {
        if (groupId <= 0) {
            return Collections.emptyList();
        }

        List<GroupMemberView> members = membershipRepository.listMembers(groupId);
        List<Integer> userIds = new ArrayList<>(members.size());
        for (GroupMemberView member : members) {
            userIds.add(member.userId());
        }
        return Collections.unmodifiableList(userIds);
    }

    public List<Integer> getGroupIdsForUser(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }

        List<Integer> groupIds = new ArrayList<>();
        String sql = "SELECT group_id FROM group_members WHERE user_id = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groupIds.add(rs.getInt("group_id"));
                }
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(groupIds);
    }
}
