package com.splitms.services;

public record ServiceResult<T>(boolean success, String message, T data) {

    public static <T> ServiceResult<T> ok(String message, T data) {
        return new ServiceResult<>(true, message, data);
    }

    public static <T> ServiceResult<T> fail(String message) {
        return new ServiceResult<>(false, message, null);
    }
}
