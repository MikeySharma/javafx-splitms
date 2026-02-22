package com.splitms.services;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserServiceTest {

    private static final String DEFAULT_NAME = "John Doe";
    private static final String DEFAULT_EMAIL = "john@example.com";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String INVALID_EMAIL = "wrong@example.com";
    private static final String INVALID_PASSWORD = "wrongpassword";
    private static final String SECONDARY_EMAIL = "jane@example.com";

    private UserService user;

    @Before
    public void setUp() {
        user = new UserService();
        // Delete test user if they exist from previous test runs
        try {
            UserService.deleteByEmail(DEFAULT_EMAIL);
            UserService.deleteByEmail(SECONDARY_EMAIL);
        } catch (Exception e) {
            // User doesn't exist yet, that's fine
        }
        user.register(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    @Test
    public void testDefaultConstructor() {
        UserService defaultUser = new UserService();
        assertNotNull("User should not be null", defaultUser);
        assertUserFields(defaultUser, "", "");
    }

    @Test
    public void testParameterizedConstructor() {
        UserService created = createUser(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
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
        UserService newUser = new UserService();
        boolean result = newUser.register("Jane Doe", SECONDARY_EMAIL, "pass456");
        assertTrue("Registration should succeed", result);
        assertUserFields(newUser, "Jane Doe", SECONDARY_EMAIL);
    }
    
    @Test
    public void testGetUserName() {
        assertEquals("getUserName should return correct name", DEFAULT_NAME, user.getUserName());
    }

    @Test
    public void testGetUserEmail() {
        assertEquals("getUserEmail should return correct email", DEFAULT_EMAIL, user.getUserEmail());
    }

    private static UserService createUser(String name, String email, String password) {
        return new UserService(name, email, password);
    }

    private static void assertUserFields(UserService user, String expectedName, String expectedEmail) {
        assertEquals("Name should match", expectedName, user.getUserName());
        assertEquals("Email should match", expectedEmail, user.getUserEmail());
    }
}
