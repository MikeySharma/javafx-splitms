package com.splitms.utils;

public class Normalize {
    public static String normalizeText(String input) {
        return input == null ? "" : input.trim();
    }

    public static String normalizeEmail(String email) {
        return normalizeText(email).toLowerCase();
    }
}
