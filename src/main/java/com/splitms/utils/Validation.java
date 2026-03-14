package com.splitms.utils;

public class Validation {

    public static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.');
    }

    public static String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

}
