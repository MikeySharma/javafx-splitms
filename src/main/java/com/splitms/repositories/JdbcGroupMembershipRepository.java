package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.GroupMemberView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcGroupMembershipRepository implements GroupMembershipRepository {

    @Override
    public List<GroupMemberView> listMembers(int groupId) {
        String sql = """
                SELECT u.id, u.name, u.email
                FROM group_members gm
                JOIN users u ON u.id = gm.user_id
                WHERE gm.group_id = ?
                ORDER BY u.name ASC
                """;

        List<GroupMemberView> members = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new GroupMemberView(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email")));
                }
            }
            return members;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list group members", e);
        }
    }

    @Override
    public boolean addMember(int groupId, int userId) {
        String sql = "INSERT IGNORE INTO group_members(group_id, user_id) VALUES (?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add group member", e);
        }
    }

    @Override
    public boolean removeMember(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove group member", e);
        }
    }

    @Override
    public boolean isMember(int groupId, int userId) {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ? LIMIT 1";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check group membership", e);
        }
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
