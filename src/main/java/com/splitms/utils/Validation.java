package com.splitms.utils;

public class Validation {
    
    public static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.');
    }
}
