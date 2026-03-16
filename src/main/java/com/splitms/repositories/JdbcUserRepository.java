package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<UserAuthRecord> findAuthByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash FROM users WHERE email = ? LIMIT 1";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UserAuthRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to lookup auth user", e);
        }
    }

    @Override
    public Optional<UserAccount> findById(int userId) {
        String sql = "SELECT id, name, email FROM users WHERE id = ? LIMIT 1";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UserAccount(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by id", e);
        }
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        String sql = "SELECT id, name, email FROM users WHERE email = ? LIMIT 1";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UserAccount(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by email", e);
        }
    }

    @Override
    public int create(String name, String email, String passwordHash) {
        String sql = "INSERT INTO users(name, email, password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
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
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public boolean updateProfile(int userId, String name, String email) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    @Override
    public boolean existsByEmailExcludingUser(String email, int userId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND id <> ? LIMIT 1";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check duplicate email", e);
        }
    }

    @Override
    public void deleteByEmail(String email) {
        String sql = "DELETE FROM users WHERE email = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user by email", e);
        }
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
