package com.splitms.services;

import com.splitms.lib.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    // variable declarations
    private Long userId;
    private String name;
    private String email;

    // default constructor
    public User() {
        this.userId = null;
        this.name = "";
        this.email = "";
    }

    // parameterized constructor
    public User(String name, String email, String password) {
        this.userId = null;
        this.name = name;
        this.email = email;
    }

    // helper method to hash passwords (for demonstration purposes only)
    private static String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }

    // login method
    public long login(String email, String password) {
        String hashed = hashPassword(password);

        try (Connection connection = Database.open();
                PreparedStatement statement = connection.prepareStatement(
                        "select id, name, email, password_hash from users where email = ?")) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && hashed.equals(rs.getString("password_hash"))) {
                    this.userId = rs.getLong("id");
                    this.name = rs.getString("name");
                    this.email = rs.getString("email");
                    return this.userId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }

        return -1;
    }

    // register method
    public boolean register(String name, String email, String password) {
        String hashed = hashPassword(password);

        try (Connection connection = Database.open();
                PreparedStatement statement = connection.prepareStatement(
                        "insert into users (name, email, password_hash) values (?, ?, ?) returning id")) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashed);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    this.userId = rs.getLong("id");
                    this.name = name;
                    this.email = email;
                    return true;
                }
            }
        } catch (SQLException | IllegalStateException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Delete a user by email (for testing purposes)
     */
    public static void deleteByEmail(String email) throws SQLException {
        try (Connection connection = Database.open();
                PreparedStatement statement = connection.prepareStatement(
                        "delete from users where email = ?")) {
            statement.setString(1, email);
            statement.executeUpdate();
        }
    }

    // getUserName method
    public String getUserName() {
        return this.name;
    }

    // getUserEmail method
    public String getUserEmail() {
        return this.email;
    }

}
