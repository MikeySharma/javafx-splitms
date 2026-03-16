package com.splitms.interfaces;

import com.splitms.models.UserAccount;
import com.splitms.services.ServiceResult;

public interface UserAuthService {
    ServiceResult<UserAccount> login(String email, String password);

    ServiceResult<UserAccount> register(String name, String email, String password);
}
