package com.splitms.services;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    private static final String DEFAULT_NAME = "John Doe";
    private static final String DEFAULT_EMAIL = "john@example.com";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String INVALID_EMAIL = "wrong@example.com";
    private static final String INVALID_PASSWORD = "wrongpassword";

    private User user;

    @Before
    public void setUp() {
        user = new User();
        // Delete test user if they exist from previous test runs
        try {
            User.deleteByEmail(DEFAULT_EMAIL);
        } catch (Exception e) {
            // User doesn't exist yet, that's fine
        }
        user.register(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull("User should not be null", defaultUser);
        assertUserFields(defaultUser, "", "");
    }

    @Test
    public void testParameterizedConstructor() {
        User created = createUser(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        assertUserFields(created, DEFAULT_NAME, DEFAULT_EMAIL);
    }

    @Test
    public void testLoginWithValidCredentials() {
        long result = user.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        assertTrue("Login should return userId >= 0", result >= 0);
    }

    @Test
    public void testLoginWithInvalidEmail() {
        long result = user.login(INVALID_EMAIL, DEFAULT_PASSWORD);
        assertEquals("Login should return -1 on invalid email", -1, result);
    }

    @Test
    public void testLoginWithInvalidPassword() {
        long result = user.login(DEFAULT_EMAIL, INVALID_PASSWORD);
        assertEquals("Login should return -1 on invalid password", -1, result);
    }

    @Test
    public void testRegister() {
        User newUser = new User();
        boolean result = newUser.register("Jane Doe", "jane@example.com", "pass456");
        assertTrue("Registration should succeed", result);
        assertUserFields(newUser, "Jane Doe", "jane@example.com");
    }
    
    @Test
    public void testGetUserName() {
        assertEquals("getUserName should return correct name", DEFAULT_NAME, user.getUserName());
    }

    @Test
    public void testGetUserEmail() {
        assertEquals("getUserEmail should return correct email", DEFAULT_EMAIL, user.getUserEmail());
    }

    private static User createUser(String name, String email, String password) {
        return new User(name, email, password);
    }

    private static void assertUserFields(User user, String expectedName, String expectedEmail) {
        assertEquals("Name should match", expectedName, user.getUserName());
        assertEquals("Email should match", expectedEmail, user.getUserEmail());
    }
}
