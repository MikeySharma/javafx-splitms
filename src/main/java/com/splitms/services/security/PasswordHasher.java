package com.splitms.services.security;

public interface PasswordHasher {
    String hash(String plainTextPassword);

    boolean matches(String plainTextPassword, String storedHash);
}
