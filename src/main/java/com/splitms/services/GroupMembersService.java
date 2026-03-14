package com.splitms.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.splitms.lib.Database;

public class GroupMembersService {

    // Add a user (friend) to a group
    public boolean addMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return false;
        }

        String sql = "INSERT IGNORE INTO group_members (group_id, user_id) VALUES ("
                + groupId + ", " + userId + ")";

        try {
            int affectedRows = Database.executeUpdate(sql);
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // Remove a user (friend) from a group
    public boolean removeMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return false;
        }

        String sql = "DELETE FROM group_members WHERE group_id = " + groupId
                + " AND user_id = " + userId;

        try {
            int affectedRows = Database.executeUpdate(sql);
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // Check if a user is a member of a group
    public boolean isMember(int groupId, int userId) {
        if (groupId <= 0 || userId <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM group_members WHERE group_id = " + groupId
                + " AND user_id = " + userId + " LIMIT 1";

        try (ResultSet rs = Database.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // List all user IDs that are friends/members of a group
    public List<Integer> getMemberUserIds(int groupId) {
        if (groupId <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT user_id FROM group_members WHERE group_id = " + groupId;
        List<Integer> userIds = new ArrayList<>();

        try (ResultSet rs = Database.executeQuery(sql)) {
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(userIds);
    }

    // List all group IDs a user belongs to as a friend/member
    public List<Integer> getGroupIdsForUser(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT group_id FROM group_members WHERE user_id = " + userId;
        List<Integer> groupIds = new ArrayList<>();

        try (ResultSet rs = Database.executeQuery(sql)) {
            while (rs.next()) {
                groupIds.add(rs.getInt("group_id"));
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(groupIds);
    }
}
