package com.splitms.utils;

import com.splitms.lib.Database;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.repositories.JdbcCategoryRepository;
import com.splitms.repositories.JdbcUserRepository;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ExpensesService;
import com.splitms.services.GroupMembersService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data seeder script to populate test data for quick feature testing.
 * 
 * Usage: mvn exec:java -Dexec.mainClass="com.splitms.utils.DataSeeder"
 * 
 * Creates:
 * - Main user: mikey@mikey.com (password: mikey)
 * - 3 test members: alice@test.com, bob@test.com, charlie@test.com
 * - 3 sample groups with members
 * - Sample expenses with splits
 */
public class DataSeeder {

    private static final String MAIN_EMAIL = "mikey@mikey.com";
    private static final String MAIN_PASSWORD = "mikey";
    private static final String MAIN_NAME = "Mikey";

    private static final String[] TEST_MEMBERS = {
            "Alice Smith|alice@test.com",
            "Bob Johnson|bob@test.com",
            "Charlie Brown|charlie@test.com"
    };

    public static void main(String[] args) {
        try {
            System.out.println("🌱 Starting data seeding...");
            System.out.println();

            // Initialize database connection
            Database.initialize();
            System.out.println("✓ Database connected");

            // Clean up existing main user and test members (optional, for re-seeding)
            cleanupExistingData();

            // 1. Register main user
            int mainUserId = registerMainUser();
            if (mainUserId <= 0) {
                System.err.println("✗ Failed to register main user");
                System.exit(1);
            }
            System.out.println("✓ Registered main user: " + MAIN_EMAIL);

            // 2. Register test members
            List<Integer> testMemberIds = registerTestMembers();
            System.out.println("✓ Registered " + testMemberIds.size() + " test members");

            // 3. Populate personal group with expenses
            populatePersonalGroupExpenses(mainUserId);

            // 4. Create groups and add members
            createGroupsWithMembers(mainUserId, testMemberIds);

            System.out.println();
            System.out.println("✅ Data seeding completed successfully!");
            System.out.println();
            System.out.println("📝 Test Account Details:");
            System.out.println("  Email: " + MAIN_EMAIL);
            System.out.println("  Password: " + MAIN_PASSWORD);
            System.out.println();
            System.out.println("🧑‍🤝‍🧑 Test Members:");
            for (String member : TEST_MEMBERS) {
                String[] parts = member.split("\\|");
                System.out.println("  - " + parts[0] + " (" + parts[1] + ")");
            }

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Clean up existing test data for re-seeding.
     */
    private static void cleanupExistingData() {
        try {
            JdbcUserRepository userRepo = new JdbcUserRepository();
            userRepo.deleteByEmail(Normalize.normalizeEmail(MAIN_EMAIL));
            // Also clean up test members
            for (String member : TEST_MEMBERS) {
                String[] parts = member.split("\\|");
                userRepo.deleteByEmail(Normalize.normalizeEmail(parts[1]));
            }
            System.out.println("✓ Cleaned up existing test data");
        } catch (Exception e) {
            System.out.println("⚠ Cleanup skipped: " + e.getMessage());
        }
    }

    /**
     * Register the main test user.
     */
    private static int registerMainUser() {
        UserService userService = ApplicationServices.userService();
        ServiceResult<?> result = userService.register(MAIN_NAME, MAIN_EMAIL, MAIN_PASSWORD);
        if (!result.success()) {
            System.err.println("✗ Registration failed: " + result.message());
            return -1;
        }
        return getUserIdByEmail(MAIN_EMAIL);
    }

    /**
     * Register test member accounts.
     */
    private static List<Integer> registerTestMembers() {
        List<Integer> memberIds = new ArrayList<>();
        UserService userService = ApplicationServices.userService();

        for (String member : TEST_MEMBERS) {
            String[] parts = member.split("\\|");
            String name = parts[0];
            String email = parts[1];
            String password = "password123"; // Simple password for test accounts

            ServiceResult<?> result = userService.register(name, email, password);
            if (!result.success()) {
                System.err.println("⚠ Failed to register " + email + ": " + result.message());
                continue;
            }

            int userId = getUserIdByEmail(email);
            if (userId > 0) {
                memberIds.add(userId);
            }
        }

        return memberIds;
    }

    /**
     * Populate the personal group (created on registration) with sample expenses.
     */
    private static void populatePersonalGroupExpenses(int mainUserId) {
        ExpensesService expensesService = ApplicationServices.expensesService();
        Map<String, Integer> categoryIdsByName = loadCategoryIdsByName();

        int personalGroupId = getGroupIdByName(mainUserId, "Personal Group");
        if (personalGroupId <= 0) {
            return;
        }

        System.out.println("✓ Populating Personal Group expenses");

        // Add some personal expenses
        createExpense(expensesService, personalGroupId, mainUserId,
            getCategoryId(categoryIdsByName, "Food & Dining"), "Coffee Purchase",
                new BigDecimal("150"), List.of(mainUserId), "Daily coffee from café");

        createExpense(expensesService, personalGroupId, mainUserId,
            getCategoryId(categoryIdsByName, "Groceries"), "Groceries",
                new BigDecimal("2500"), List.of(mainUserId), "Weekly grocery shopping");

        createExpense(expensesService, personalGroupId, mainUserId,
            getCategoryId(categoryIdsByName, "Phone & Internet"), "Internet Bill",
                new BigDecimal("1200"), List.of(mainUserId), "Monthly internet subscription");
    }

    /**
     * Create sample groups and add members.
     */
    private static void createGroupsWithMembers(int mainUserId, List<Integer> testMemberIds) {
        GroupsService groupsService = ApplicationServices.groupsService();
        GroupMembersService membersService = ApplicationServices.groupMembersService();
        ExpensesService expensesService = ApplicationServices.expensesService();

        // Define sample groups
        String[][] groups = {
                {"Roommate Expenses", "Shared apartment costs and household items"},
                {"Weekend Trip", "Trip to Goa - accommodation, food, activities"},
                {"Office Meals", "Team lunches and coffee expenses"}
        };

        int groupCount = 0;
        for (String[] groupInfo : groups) {
            String groupName = groupInfo[0];
            String description = groupInfo[1];

            ServiceResult<?> createResult = groupsService.createGroup(mainUserId, groupName, description);
            if (!createResult.success()) {
                System.err.println("⚠ Failed to create group: " + groupName);
                continue;
            }

            // Get the created group ID
            int groupId = getGroupIdByName(mainUserId, groupName);
            if (groupId <= 0) {
                continue;
            }

            // Add test members to group
            for (int memberId : testMemberIds) {
                if (memberId != mainUserId) {
                    boolean added = membersService.addMember(groupId, memberId);
                    if (!added) {
                        System.out.println("⚠ Could not add member to " + groupName);
                    }
                }
            }

            System.out.println("✓ Created group: " + groupName + " with " + testMemberIds.size()
                    + " members");

            // Create sample expenses based on group
            createSampleExpenses(groupId, mainUserId, testMemberIds, groupName, expensesService);
            groupCount++;
        }

        System.out.println("✓ Created " + groupCount + " groups with sample expenses");
    }

    /**
     * Create sample expenses for a group.
     */
    private static void createSampleExpenses(int groupId, int mainUserId, List<Integer> testMemberIds,
            String groupName, ExpensesService expensesService) {

        if (testMemberIds.isEmpty()) {
            System.out.println("  ⚠ Skipping expenses: no test members available");
            return;
        }

        Map<String, Integer> categoryIdsByName = loadCategoryIdsByName();

        if ("Roommate Expenses".equals(groupName)) {
            // Rent split
            createExpense(expensesService, groupId, mainUserId,
                getCategoryId(categoryIdsByName, "Rent & Housing"), "Monthly Rent",
                    new BigDecimal("5000"),
                    List.of(mainUserId, testMemberIds.get(0), 
                            testMemberIds.size() > 1 ? testMemberIds.get(1) : mainUserId,
                            testMemberIds.size() > 2 ? testMemberIds.get(2) : mainUserId),
                    "Rent for Q4 apartment");

            // Groceries - only if we have at least 2 test members
            if (testMemberIds.size() >= 2) {
                createExpense(expensesService, groupId, testMemberIds.get(0),
                    getCategoryId(categoryIdsByName, "Groceries"), "Groceries",
                        new BigDecimal("1200"),
                        List.of(mainUserId, testMemberIds.get(0), testMemberIds.get(1)),
                        "Weekly groceries from supermarket");
            }

        } else if ("Weekend Trip".equals(groupName)) {
            // Accommodation
                createExpense(expensesService, groupId, mainUserId,
                    getCategoryId(categoryIdsByName, "Travel"), "Hotel Booking",
                    new BigDecimal("2400"),
                    List.of(mainUserId, testMemberIds.get(0), 
                            testMemberIds.size() > 1 ? testMemberIds.get(1) : mainUserId,
                            testMemberIds.size() > 2 ? testMemberIds.get(2) : mainUserId),
                    "3 nights at resort");

            // Activities - only if we have at least 2 test members
            if (testMemberIds.size() >= 2) {
                createExpense(expensesService, groupId, testMemberIds.get(1),
                    getCategoryId(categoryIdsByName, "Entertainment"),
                        "Adventure Activities",
                        new BigDecimal("900"),
                        List.of(mainUserId, testMemberIds.get(0), testMemberIds.get(1),
                                testMemberIds.size() > 2 ? testMemberIds.get(2) : mainUserId),
                        "Scuba diving and water sports");
            }

        } else if ("Office Meals".equals(groupName)) {
            // Lunch
            createExpense(expensesService, groupId, mainUserId,
                getCategoryId(categoryIdsByName, "Food & Dining"), "Team Lunch",
                    new BigDecimal("900"),
                    List.of(mainUserId, testMemberIds.get(0), 
                            testMemberIds.size() > 1 ? testMemberIds.get(1) : mainUserId),
                    "Friday team lunch at Thai restaurant");

            // Snacks
            createExpense(expensesService, groupId, testMemberIds.get(0),
                getCategoryId(categoryIdsByName, "Food & Dining"),
                    "Coffee & Snacks",
                    new BigDecimal("350"),
                    List.of(mainUserId, testMemberIds.get(0), 
                            testMemberIds.size() > 1 ? testMemberIds.get(1) : mainUserId,
                            testMemberIds.size() > 2 ? testMemberIds.get(2) : mainUserId),
                    "Weekly team snacks");
        }
    }

    private static Map<String, Integer> loadCategoryIdsByName() {
        JdbcCategoryRepository categoryRepo = new JdbcCategoryRepository();
        Map<String, Integer> categoryIdsByName = new HashMap<>();

        categoryRepo.findAll().forEach(category ->
                categoryIdsByName.put(category.categoryName(), category.categoryId()));

        return categoryIdsByName;
    }

    private static int getCategoryId(Map<String, Integer> categoryIdsByName, String categoryName) {
        if (categoryIdsByName.containsKey(categoryName)) {
            return categoryIdsByName.get(categoryName);
        }

        // Fallback to first known category ID, otherwise 1.
        return categoryIdsByName.values().stream().findFirst().orElse(1);
    }

    /**
     * Helper to create expense with equal splits among members.
     */
    private static void createExpense(ExpensesService expensesService, int groupId, int payerId,
            int categoryId, String title, BigDecimal amount, List<Integer> memberIds,
            String description) {

        // Calculate equal split
        BigDecimal splitAmount = amount.divide(BigDecimal.valueOf(memberIds.size()), 2,
                RoundingMode.HALF_UP);
        float splitPercentage = 100.0f / memberIds.size();

        // Create splits for each member
        List<ExpenseSplitModel> splits = new ArrayList<>();
        for (int memberId : memberIds) {
            splits.add(new ExpenseSplitModel(0, 0, memberId, splitAmount, splitPercentage));
        }

        ServiceResult<?> result = expensesService.createExpenseWithSplits(
                groupId, payerId, categoryId, amount, LocalDate.now(), title, description, splits);

        if (result.success()) {
            System.out.println("  ✓ Expense: " + title + " (NPR " + amount + ") split among "
                    + memberIds.size() + " members");
        } else {
            System.out.println("  ⚠ Failed to create expense: " + title + " - " + result.message());
        }
    }

    /**
     * Get user ID by email.
     */
    private static int getUserIdByEmail(String email) {
        try {
            JdbcUserRepository userRepo = new JdbcUserRepository();
            return userRepo.findByEmail(Normalize.normalizeEmail(email))
                    .map(account -> account.userId())
                    .orElse(-1);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get group ID by group name and owner.
     */
    private static int getGroupIdByName(int ownerId, String groupName) {
        try {
            return ApplicationServices.groupsService()
                    .listGroupsForUser(ownerId, "")
                    .data()
                    .stream()
                    .filter(g -> g.groupName().equalsIgnoreCase(groupName))
                    .map(g -> g.groupId())
                    .findFirst()
                    .orElse(-1);

        } catch (Exception e) {
            return -1;
        }
    }
}
