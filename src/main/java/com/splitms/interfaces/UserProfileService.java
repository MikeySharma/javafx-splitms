package com.splitms.interfaces;

import com.splitms.models.UserAccount;
import com.splitms.services.ServiceResult;

public interface UserProfileService {
    ServiceResult<UserAccount> getProfile(int userId);

    ServiceResult<UserAccount> updateProfile(int userId, String name, String email);
}
