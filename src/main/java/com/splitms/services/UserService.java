package com.splitms.services;

import com.splitms.interfaces.UserAuthService;
import com.splitms.interfaces.UserProfileService;
import com.splitms.models.UserAccount;
import com.splitms.repositories.GroupRepository;
import com.splitms.repositories.JdbcGroupRepository;
import com.splitms.repositories.JdbcUserRepository;
import com.splitms.repositories.UserRepository;
import com.splitms.services.security.PasswordHasher;
import com.splitms.services.security.Pbkdf2PasswordHasher;
import com.splitms.utils.Normalize;
import com.splitms.utils.Validation;
import java.util.Optional;

public class UserService implements UserAuthService, UserProfileService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembersService groupMembersService;
    private final PasswordHasher passwordHasher;

    public UserService() {
        this(new JdbcUserRepository(), new JdbcGroupRepository(), null, new Pbkdf2PasswordHasher());
    }

    public UserService(UserRepository userRepository, GroupRepository groupRepository, PasswordHasher passwordHasher) {
        this(userRepository, groupRepository, null, passwordHasher);
    }

    public UserService(UserRepository userRepository, GroupRepository groupRepository, GroupMembersService groupMembersService, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMembersService = groupMembersService;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public ServiceResult<UserAccount> login(String email, String password) {
        String normalizedEmail = Normalize.normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.isBlank()) {
            return ServiceResult.fail("Email and password are required.");
        }

        Optional<UserRepository.UserAuthRecord> authRecord = userRepository.findAuthByEmail(normalizedEmail);
        if (authRecord.isEmpty()) {
            return ServiceResult.fail("Invalid email or password.");
        }

        UserRepository.UserAuthRecord record = authRecord.get();
        if (!passwordHasher.matches(password, record.passwordHash())) {
            return ServiceResult.fail("Invalid email or password.");
        }

        UserAccount account = new UserAccount(record.userId(), record.name(), record.email());
        return ServiceResult.ok("Login successful.", account);
    }

    @Override
    public ServiceResult<UserAccount> register(String name, String email, String password) {
        String normalizedName = Normalize.normalizeText(name);
        String normalizedEmail = Normalize.normalizeEmail(email);

        if (normalizedName.isBlank() || normalizedEmail.isBlank() || password == null || password.isBlank()) {
            return ServiceResult.fail("Please fill in all fields.");
        }

        if (!Validation.isValidEmail(normalizedEmail)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return ServiceResult.fail("Email already exists.");
        }

        String passwordHash = passwordHasher.hash(password);
        int userId = userRepository.create(normalizedName, normalizedEmail, passwordHash);
        if (userId <= 0) {
            return ServiceResult.fail("Registration failed.");
        }

        int groupId = groupRepository.create(
                userId,
                "Personal Group",
                "This is your personal default group.",
                true);

        if (groupId <= 0) {
            return ServiceResult.fail("Account created, but default group could not be created.");
        }

        // Automatically add the user as a member of their default personal group
        if (groupMembersService != null) {
            groupMembersService.addMember(groupId, userId);
        }

        return ServiceResult.ok("Registration successful.", new UserAccount(userId, normalizedName, normalizedEmail));
    }

    @Override
    public ServiceResult<UserAccount> getProfile(int userId) {
        if (userId <= 0) {
            return ServiceResult.fail("Invalid user id.");
        }

        return userRepository.findById(userId)
                .map(account -> ServiceResult.ok("Profile loaded.", account))
                .orElseGet(() -> ServiceResult.fail("User not found."));
    }

    @Override
    public ServiceResult<UserAccount> updateProfile(int userId, String name, String email) {
        String normalizedName = Normalize.normalizeText(name);
        String normalizedEmail = Normalize.normalizeEmail(email);

        if (userId <= 0 || normalizedName.isBlank() || normalizedEmail.isBlank()) {
            return ServiceResult.fail("Name and email are required.");
        }

        if (!Validation.isValidEmail(normalizedEmail)) {
            return ServiceResult.fail("Please enter a valid email address.");
        }

        if (userRepository.existsByEmailExcludingUser(normalizedEmail, userId)) {
            return ServiceResult.fail("Email is already in use by another account.");
        }

        boolean updated = userRepository.updateProfile(userId, normalizedName, normalizedEmail);
        if (!updated) {
            return ServiceResult.fail("Profile update failed.");
        }

        return ServiceResult.ok("Profile updated.", new UserAccount(userId, normalizedName, normalizedEmail));
    }

    public static void deleteByEmail(String email) {
        new JdbcUserRepository().deleteByEmail(Normalize.normalizeEmail(email));
    }
}
