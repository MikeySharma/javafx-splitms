package com.splitms.services;

import com.splitms.models.UserAccount;
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

    private UserService userService;
    private int registeredUserId;

    @Before
    public void setUp() {
        userService = new UserService();
        try {
            UserService.deleteByEmail(DEFAULT_EMAIL);
            UserService.deleteByEmail(SECONDARY_EMAIL);
        } catch (Exception e) {
            // No-op for cleanup
        }

        ServiceResult<UserAccount> registerResult = userService.register(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        assertTrue("Default user registration should succeed", registerResult.success());
        assertNotNull("Registration should return account", registerResult.data());
        registeredUserId = registerResult.data().userId();
    }

    @Test
    public void testDefaultConstructor() {
        UserService defaultUser = new UserService();
        assertNotNull("User should not be null", defaultUser);
    }

    @Test
    public void testLoginWithValidCredentials() {
        ServiceResult<UserAccount> result = userService.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        assertTrue("Login should succeed", result.success());
        assertNotNull("Login should include account", result.data());
        assertTrue("Login should return userId > 0", result.data().userId() > 0);
    }

    @Test
    public void testLoginWithInvalidEmail() {
        ServiceResult<UserAccount> result = userService.login(INVALID_EMAIL, DEFAULT_PASSWORD);
        assertFalse("Login should fail with invalid email", result.success());
    }

    @Test
    public void testLoginWithInvalidPassword() {
        ServiceResult<UserAccount> result = userService.login(DEFAULT_EMAIL, INVALID_PASSWORD);
        assertFalse("Login should fail with invalid password", result.success());
    }

    @Test
    public void testRegister() {
        ServiceResult<UserAccount> result = userService.register("Jane Doe", SECONDARY_EMAIL, "pass456");
        assertTrue("Registration should succeed", result.success());
        assertNotNull("Registration should return account", result.data());
        assertEquals("Name should match", "Jane Doe", result.data().name());
        assertEquals("Email should match", SECONDARY_EMAIL, result.data().email());
    }

    @Test
    public void testGetProfile() {
        ServiceResult<UserAccount> profileResult = userService.getProfile(registeredUserId);
        assertTrue("Profile lookup should succeed", profileResult.success());
        assertNotNull("Profile result should include account", profileResult.data());
        assertEquals("Profile name should match", DEFAULT_NAME, profileResult.data().name());
        assertEquals("Profile email should match", DEFAULT_EMAIL, profileResult.data().email());
    }

    @Test
    public void testUpdateProfile() {
        String updatedEmail = "john-updated-" + System.nanoTime() + "@example.com";
        ServiceResult<UserAccount> updateResult = userService.updateProfile(
                registeredUserId,
                "John Updated",
            updatedEmail);

        assertTrue("Profile update should succeed", updateResult.success());
        assertNotNull("Profile update should return updated account", updateResult.data());
        assertEquals("Updated name should match", "John Updated", updateResult.data().name());
        assertEquals("Updated email should match", updatedEmail, updateResult.data().email());
    }
}
