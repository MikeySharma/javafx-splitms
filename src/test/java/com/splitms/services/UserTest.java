package com.splitms.services;

public class UserTest {

    public static void main(String[] args) {
        UserTest test = new UserTest();
        test.testDefaultConstructor();
        test.testParameterizedConstructor();
        test.testLoginWithValidCredentials();
        test.testLoginWithInvalidEmail();
        test.testLoginWithInvalidPassword();
        test.testRegister();
        test.testHashPassword();
        test.testGetUserName();
        test.testGetUserEmail();
        System.out.println("All tests passed!");
    }

    public void testDefaultConstructor() {
        User defaultUser = new User();
        assert defaultUser != null : "User should not be null";
        assert defaultUser.getUserName().equals("") : "Default name should be empty";
        assert defaultUser.getUserEmail().equals("") : "Default email should be empty";
        System.out.println("✓ testDefaultConstructor passed");
    }

    public void testParameterizedConstructor() {
        User user = new User("John Doe", "john@example.com", "password123");
        assert user.getUserName().equals("John Doe") : "Name should match";
        assert user.getUserEmail().equals("john@example.com") : "Email should match";
        System.out.println("✓ testParameterizedConstructor passed");
    }

    public void testLoginWithValidCredentials() {
        User user = new User("John Doe", "john@example.com", "password123");
        boolean result = user.login("john@example.com", "password123");
        assert result : "Login should succeed with valid credentials";
        System.out.println("✓ testLoginWithValidCredentials passed");
    }

    public void testLoginWithInvalidEmail() {
        User user = new User("John Doe", "john@example.com", "password123");
        boolean result = user.login("wrong@example.com", "password123");
        assert !result : "Login should fail with invalid email";
        System.out.println("✓ testLoginWithInvalidEmail passed");
    }

    public void testLoginWithInvalidPassword() {
        User user = new User("John Doe", "john@example.com", "password123");
        boolean result = user.login("john@example.com", "wrongpassword");
        assert !result : "Login should fail with invalid password";
        System.out.println("✓ testLoginWithInvalidPassword passed");
    }

    public void testRegister() {
        User newUser = new User();
        boolean result = newUser.register("Jane Doe", "jane@example.com", "pass456");
        assert result : "Registration should succeed";
        assert newUser.getUserName().equals("Jane Doe") : "Name should match after registration";
        assert newUser.getUserEmail().equals("jane@example.com") : "Email should match after registration";
        System.out.println("✓ testRegister passed");
    }

    public void testHashPassword() {
        String hashed = User.hashPassword("password123");
        assert hashed != null : "Hashed password should not be null";
        assert !hashed.isEmpty() : "Hashed password should not be empty";
        System.out.println("✓ testHashPassword passed");
    }

    public void testGetUserName() {
        User user = new User("John Doe", "john@example.com", "password123");
        assert user.getUserName().equals("John Doe") : "getUserName should return correct name";
        System.out.println("✓ testGetUserName passed");
    }

    public void testGetUserEmail() {
        User user = new User("John Doe", "john@example.com", "password123");
        assert user.getUserEmail().equals("john@example.com") : "getUserEmail should return correct email";
        System.out.println("✓ testGetUserEmail passed");
    }
}
