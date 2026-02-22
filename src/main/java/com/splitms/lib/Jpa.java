package com.splitms.lib;

import com.splitms.utils.EnvConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Jpa {

    static {
        quietHibernateLogs();
    }

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            Persistence.createEntityManagerFactory("splitms", buildOverrides());

    private Jpa() {
    }

    private static Map<String, Object> buildOverrides() {
        String host = EnvConfig.getEnvOrDefault("DB_HOST", "localhost");
        String port = EnvConfig.getEnvOrDefault("DB_PORT", "3306");
        String name = EnvConfig.getEnvOrDefault("DB_NAME", "splitms");
        String user = EnvConfig.getEnvOrDefault("DB_USER", "splitms");
        String password = EnvConfig.getEnvOrDefault("DB_PASSWORD", "splitms");

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("jakarta.persistence.jdbc.url",
            "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", password);
        return properties;
    }

    private static void quietHibernateLogs() {
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        Logger.getLogger("org.jboss").setLevel(Level.SEVERE);
    }

    public static EntityManager openEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    public static Map<String, String> fetchDatabaseInfo() {
        Map<String, String> info = new HashMap<>();

        try (EntityManager entityManager = openEntityManager()) {
            Object[] result = (Object[]) entityManager
                    .createNativeQuery("select database(), current_user(), version()")
                    .getSingleResult();

            info.put("database", result[0].toString());
            info.put("user", result[1].toString());
            info.put("version", result[2].toString());
        }

        return info;
    }

    public static void shutdown() {
        if (ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }
}
