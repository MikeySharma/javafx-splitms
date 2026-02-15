package com.splitms.services;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("John Doe", "john@example.com", "password123");
    }

    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull("User should not be null", defaultUser);
        assertEquals("Default name should be empty", "", defaultUser.getUserName());
        assertEquals("Default email should be empty", "", defaultUser.getUserEmail());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("Name should match", "John Doe", user.getUserName());
        assertEquals("Email should match", "john@example.com", user.getUserEmail());
    }

    @Test
    public void testLoginWithValidCredentials() {
        int result = user.login("john@example.com", "password123");
        assertTrue("Login should return userId >= 0", result >= 0);
    }

    @Test
    public void testLoginWithInvalidEmail() {
        int result = user.login("wrong@example.com", "password123");
        assertEquals("Login should return -1 on invalid email", -1, result);
    }

    @Test
    public void testLoginWithInvalidPassword() {
        int result = user.login("john@example.com", "wrongpassword");
        assertEquals("Login should return -1 on invalid password", -1, result);
    }

    @Test
    public void testRegister() {
        User newUser = new User();
        boolean result = newUser.register("Jane Doe", "jane@example.com", "pass456");
        assertTrue("Registration should succeed", result);
        assertEquals("Name should match after registration", "Jane Doe", newUser.getUserName());
        assertEquals("Email should match after registration", "jane@example.com", newUser.getUserEmail());
    }

    @Test
    public void testHashPassword() {
        String hashed = User.hashPassword("password123");
        assertNotNull("Hashed password should not be null", hashed);
        assertFalse("Hashed password should not be empty", hashed.isEmpty());
    }

    @Test
    public void testGetUserName() {
        assertEquals("getUserName should return correct name", "John Doe", user.getUserName());
    }

    @Test
    public void testGetUserEmail() {
        assertEquals("getUserEmail should return correct email", "john@example.com", user.getUserEmail());
    }
}
