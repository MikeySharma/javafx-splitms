package com.splitms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class EnvConfig {

    private static final Map<String, String> envVariables = new HashMap<>();

    static {
        loadEnvFile();
    }

    private EnvConfig() {
    }

    private static void loadEnvFile() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) {
            System.err.println("Warning: .env file not found at " + envPath.toAbsolutePath());
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(envPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Parse KEY=VALUE format
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    // Remove quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envVariables.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading .env file: " + e.getMessage());
        }
    }

    public static String get(String name) {
        return envVariables.get(name);
    }

    public static String getRequiredEnv(String name) {
        String value = envVariables.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

    public static String getEnvOrDefault(String name, String fallback) {
        String value = envVariables.get(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
