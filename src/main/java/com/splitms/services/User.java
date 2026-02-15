package com.splitms.services;

import java.util.UUID;

public class User {

    // variable declarations
    private int userId;
    private String name;
    private String email;
    private String passwordHash;

    // default constructor
    public User() {
        this.userId = UUID.randomUUID().hashCode();
        this.name = "";
        this.email = "";
        this.passwordHash = "";
    }

    // parameterized constructor
    public User(String name, String email, String passwordHash) {
        this.userId = UUID.randomUUID().hashCode();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // login method
    public boolean login(String email, String passwordHash) {
        // In a real application, you would check the email and passwordHash against a
        // database
        return this.email.equals(email) && this.passwordHash.equals(passwordHash);
    }

    // register method
    public boolean register(String name, String email, String passwordHash) {
        // In a real application, you would save the user details to a database
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        return true; // Assume registration is always successful for this example
    }

    // helper method to hash passwords (for demonstration purposes only, use a
    // proper hashing algorithm in production)
    public static String hashPassword(String password) {
        return Integer.toString(password.hashCode());
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
