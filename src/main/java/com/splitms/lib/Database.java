package com.splitms.lib;

import com.splitms.utils.EnvConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class Database {

    private Database() {
    }

    public static Connection open() throws SQLException {
        String host = EnvConfig.getRequiredEnv("DB_HOST");
        String port = EnvConfig.getEnvOrDefault("DB_PORT", "5432");
        String name = EnvConfig.getRequiredEnv("DB_NAME");
        String user = EnvConfig.getRequiredEnv("DB_USER");
        String password = EnvConfig.getRequiredEnv("DB_PASSWORD");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        return DriverManager.getConnection(url, user, password);
    }

    public static Map<String, String> fetchDatabaseInfo() throws SQLException {
        String sql = "select current_database(), current_user, version()";
        Map<String, String> info = new HashMap<>();

        try (Connection connection = open();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                info.put("database", rs.getString(1));
                info.put("user", rs.getString(2));
                info.put("version", rs.getString(3));
            }
        }

        return info;
    }
}