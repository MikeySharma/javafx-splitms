package com.splitms.services;

import com.splitms.lib.Database;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // variable declarations
    private Long userId;
    private String name;
    private String email;

    // default constructor
    public UserService() {
        this.userId = null;
        this.name = "";
        this.email = "";
    }

    // parameterized constructor
    public UserService(String name, String email, String password) {
        this.userId = null;
        this.name = name;
        this.email = email;
    }

    // helper method to hash passwords (for demonstration purposes only)
    private static String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }

    private static String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    // login method
    public long login(String email, String password) {
        String hashed = hashPassword(password);
        String safeEmail = escapeSql(email);
        String sql = "select id, name, email, password_hash from users where email = '" + safeEmail + "' limit 1";

        try (ResultSet rs = Database.executeQuery(sql)) {
            if (!rs.next()) {
                return -1;
            }

            if (hashed.equals(rs.getString("password_hash"))) {
                this.userId = rs.getLong("id");
                this.name = rs.getString("name");
                this.email = rs.getString("email");
                return userId;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
        }

        return -1;
    }

    // register method
    public boolean register(String name, String email, String password) {
        String hashed = hashPassword(password);
        String safeName = escapeSql(name);
        String safeEmail = escapeSql(email);
        String safeHash = escapeSql(hashed);

        String insertSql = "insert into users(name, email, password_hash) values ('"
                + safeName + "', '" + safeEmail + "', '" + safeHash + "')";

        try {
            int rows = Database.executeUpdate(insertSql);
            if (rows <= 0) {
                return false;
            }

            try (ResultSet idResult = Database.executeQuery("select last_insert_id()")) {
                if (idResult.next()) {
                    this.userId = idResult.getLong(1);
                }
            }

            this.name = name;
            this.email = email;
            return true;
        } catch (SQLException e) {
            this.userId = null;
            this.name = "";
            this.email = "";
            return false;
        }
    }

    /**
     * Delete a user by email (for testing purposes)
     */
    public static void deleteByEmail(String email) {
        String safeEmail = escapeSql(email);
        String sql = "delete from users where email = '" + safeEmail + "'";

        try {
            Database.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
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
