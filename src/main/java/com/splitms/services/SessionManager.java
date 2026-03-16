package com.splitms.services;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private int userId = -1;
    private String userName = "";
    private String userEmail = "";

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(int userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName == null ? "" : userName;
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public void logout() {
        this.userId = -1;
        this.userName = "";
        this.userEmail = "";
    }

    public void updateProfile(String userName, String userEmail) {
        this.userName = userName == null ? "" : userName;
        this.userEmail = userEmail == null ? "" : userEmail;
    }

    public boolean isLoggedIn() {
        return userId != -1;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
