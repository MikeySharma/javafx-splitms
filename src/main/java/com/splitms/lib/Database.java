package com.splitms.lib;

import com.splitms.utils.EnvConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

public final class Database {

    private static Connection connection;

    private Database() {
    }

    private static Connection createConnection() throws SQLException {
        String host = EnvConfig.getRequiredEnv("DB_HOST");
        String port = EnvConfig.getEnvOrDefault("DB_PORT", "3306");
        String name = EnvConfig.getRequiredEnv("DB_NAME");
        String user = EnvConfig.getRequiredEnv("DB_USER");
        String password = EnvConfig.getRequiredEnv("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, user, password);
    }

    public static synchronized void initialize() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        initialize();
        return connection;
    }

    public static ResultSet executeQuery(String sql) throws SQLException {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
            rowSet.populate(rs);
            return rowSet;
        }
    }

    public static int executeUpdate(String sql) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    public static Map<String, String> fetchDatabaseInfo() throws SQLException {
        String sql = "select database(), current_user(), version()";
        Map<String, String> info = new HashMap<>();

        try (ResultSet rs = executeQuery(sql)) {
            if (rs.next()) {
                info.put("database", rs.getString(1));
                info.put("user", rs.getString(2));
                info.put("version", rs.getString(3));
            }
        }

        return info;
    }

    public static synchronized void shutdown() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}