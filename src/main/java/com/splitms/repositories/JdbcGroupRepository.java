package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.GroupModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGroupRepository implements GroupRepository {

    @Override
    public List<GroupModel> listByOwner(int userId, String searchQuery) {
        StringBuilder sql = new StringBuilder("""
                SELECT g.group_id,
                       g.user_id,
                       g.group_name,
                       g.description,
                       g.is_personal_default,
                       COUNT(gm.id) AS member_count
                FROM `group` g
                LEFT JOIN group_members gm ON gm.group_id = g.group_id
                WHERE g.user_id = ?
                """);

        boolean hasSearch = searchQuery != null && !searchQuery.isBlank();
        if (hasSearch) {
            sql.append(" AND (LOWER(g.group_name) LIKE ? OR LOWER(g.description) LIKE ?)");
        }

        sql.append(" GROUP BY g.group_id, g.user_id, g.group_name, g.description, g.is_personal_default");
        sql.append(" ORDER BY g.created_at DESC");

        List<GroupModel> groups = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (hasSearch) {
                String pattern = "%" + searchQuery.toLowerCase() + "%";
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(map(rs));
                }
            }
            return groups;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list groups", e);
        }
    }

    @Override
    public Optional<GroupModel> findByIdAndOwner(int groupId, int ownerUserId) {
        String sql = """
                SELECT g.group_id,
                       g.user_id,
                       g.group_name,
                       g.description,
                       g.is_personal_default,
                       COUNT(gm.id) AS member_count
                FROM `group` g
                LEFT JOIN group_members gm ON gm.group_id = g.group_id
                WHERE g.group_id = ? AND g.user_id = ?
                GROUP BY g.group_id, g.user_id, g.group_name, g.description, g.is_personal_default
                LIMIT 1
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, ownerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load group", e);
        }
    }

    @Override
    public int create(int userId, String groupName, String description, boolean isPersonalDefault) {
        String sql = "INSERT INTO `group` (user_id, group_name, description, is_personal_default) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, groupName);
            ps.setString(3, description);
            ps.setBoolean(4, isPersonalDefault);

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                return -1;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create group", e);
        }
    }

    @Override
    public boolean update(int groupId, int ownerUserId, String groupName, String description) {
        String sql = "UPDATE `group` SET group_name = ?, description = ? WHERE group_id = ? AND user_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setString(2, description);
            ps.setInt(3, groupId);
            ps.setInt(4, ownerUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group", e);
        }
    }

    @Override
    public boolean delete(int groupId, int ownerUserId) {
        String sql = "DELETE FROM `group` WHERE group_id = ? AND user_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, ownerUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    private static GroupModel map(ResultSet rs) throws SQLException {
        return new GroupModel(
                rs.getInt("group_id"),
                rs.getInt("user_id"),
                rs.getString("group_name"),
                rs.getString("description"),
                rs.getBoolean("is_personal_default"),
                rs.getInt("member_count"));
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
