package com.splitms.repositories;

import com.splitms.models.UserAccount;
import java.util.Optional;

public interface UserRepository {

    record UserAuthRecord(int userId, String name, String email, String passwordHash) {
    }

    Optional<UserAuthRecord> findAuthByEmail(String email);

    Optional<UserAccount> findById(int userId);

    Optional<UserAccount> findByEmail(String email);

    int create(String name, String email, String passwordHash);

    boolean updateProfile(int userId, String name, String email);

    boolean existsByEmailExcludingUser(String email, int userId);

    void deleteByEmail(String email);
}
