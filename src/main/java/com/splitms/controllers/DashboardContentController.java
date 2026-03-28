package com.splitms.controllers;

import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.models.GroupModel;
import com.splitms.models.TransactionModel;
import com.splitms.models.UserAccount;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ExpensesService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import com.splitms.services.TransactionsService;
import com.splitms.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardContentController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox youOweContainer;

    @FXML
    private VBox youAreOwedContainer;

    @FXML
    private Label netBalanceLabel;

    @FXML
    private VBox recentExpensesContainer;

    @FXML
    private VBox recentTransactionsContainer;

    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final ExpensesService expensesService = ApplicationServices.expensesService();
    private final TransactionsService transactionsService = ApplicationServices.transactionsService();
    private final UserService userService = ApplicationServices.userService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load dashboard data
        loadDashboardData();
    }

    public void setWelcomeName(String name) {
        if (name == null || name.isBlank()) {
            welcomeLabel.setText("Welcome back!");
            return;
        }

        welcomeLabel.setText("Welcome back, " + name + "!");
    }

    private void loadDashboardData() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            return;
        }

        // Load and display settlement information
        loadSettlementInfo(userId);

        // Load recent expenses
        loadRecentExpenses(userId);

        // Load recent transactions
        loadRecentTransactions(userId);
    }

    private void loadSettlementInfo(int userId) {
        // Get all groups for the user
        ServiceResult<List<GroupModel>> groupsResult = groupsService.listGroupsForUser(userId, "");
        if (!groupsResult.success() || groupsResult.data() == null) {
            return;
        }

        // Get all expense splits for the user (what user owes/is owed based on splits)
        ServiceResult<List<ExpenseSplitModel>> splitsResult = expensesService.listExpenseSplitsForUser(userId);
        if (!splitsResult.success() || splitsResult.data() == null) {
            // Fallback to transaction-based if splits not available
            loadSettlementInfoFromTransactions(userId, groupsResult.data());
            return;
        }

        List<GroupModel> groups = groupsResult.data();
        List<ExpenseSplitModel> userSplits = splitsResult.data();

        // Aggregate settlement info from expense splits
        Map<Integer, SettlementInfo> settlementMap = new HashMap<>();

        // For each expense split the user participates in
        for (ExpenseSplitModel split : userSplits) {
            // Get the expense to find who paid
            ServiceResult<ExpenseModel> expenseResult = expensesService.getExpense(split.expenseId());
            if (!expenseResult.success() || expenseResult.data() == null) {
                continue;
            }

            ExpenseModel expense = expenseResult.data();
            int payerId = expense.payerId();

            // User owes the payer the split amount (unless user IS the payer)
            if (payerId != userId) {
                settlementMap.computeIfAbsent(payerId, k -> new SettlementInfo(payerId))
                        .addOwed(split.shareAmount());
            }
        }

        // For each expense paid by the user, others owe the user
        for (GroupModel group : groups) {
            ServiceResult<List<ExpenseModel>> expensesResult = expensesService.listExpensesForGroup(group.groupId());
            if (!expensesResult.success() || expensesResult.data() == null) {
                continue;
            }

            for (ExpenseModel expense : expensesResult.data()) {
                if (expense.payerId() == userId) {
                    // User paid for this expense, so others' shares are owed to the user.
                    ServiceResult<List<ExpenseSplitModel>> expenseSplitsResult =
                            expensesService.listExpenseSplitsForExpense(expense.expenseId());

                    if (!expenseSplitsResult.success() || expenseSplitsResult.data() == null) {
                        continue;
                    }

                    for (ExpenseSplitModel split : expenseSplitsResult.data()) {
                        if (split.userId() != userId) {
                            settlementMap.computeIfAbsent(split.userId(), k -> new SettlementInfo(split.userId()))
                                    .addOwing(split.shareAmount());
                        }
                    }
                }
            }
        }

        // Get user names for display
        Map<Integer, String> userNames = new HashMap<>();
        for (Integer otherId : settlementMap.keySet()) {
            userNames.put(otherId, getUserName(otherId));
        }

        // Display settlement info
        displaySettlementCards(settlementMap, userNames, userId);
    }

    private void loadSettlementInfoFromTransactions(int userId, List<GroupModel> groups) {
        // Fallback to transaction-based settlement calculation
        Map<Integer, SettlementInfo> settlementMap = new HashMap<>();

        for (GroupModel group : groups) {
            ServiceResult<List<TransactionModel>> transResult = transactionsService.listTransactionsForGroup(group.groupId());
            if (!transResult.success() || transResult.data() == null) {
                continue;
            }

            for (TransactionModel trans : transResult.data()) {
                // Only consider unsettled transactions
                if (trans.settled()) {
                    continue;
                }

                if (trans.fromUserId() == userId) {
                    // User owes toUserId
                    int toUserId = trans.toUserId();
                    settlementMap.computeIfAbsent(toUserId, k -> new SettlementInfo(toUserId))
                            .addOwed(trans.amount());
                } else if (trans.toUserId() == userId) {
                    // User is owed by fromUserId
                    int fromUserId = trans.fromUserId();
                    settlementMap.computeIfAbsent(fromUserId, k -> new SettlementInfo(fromUserId))
                            .addOwing(trans.amount());
                }
            }
        }

        // Get user names for display
        Map<Integer, String> userNames = new HashMap<>();
        for (Integer otherId : settlementMap.keySet()) {
            userNames.put(otherId, getUserName(otherId));
        }

        // Display settlement info
        displaySettlementCards(settlementMap, userNames, userId);
    }

    private void displaySettlementCards(Map<Integer, SettlementInfo> settlementMap,
                                        Map<Integer, String> userNames, int userId) {
        // Clear containers
        youOweContainer.getChildren().clear();
        youAreOwedContainer.getChildren().clear();

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalOwing = BigDecimal.ZERO;

        // Display "You Owe" section
        for (SettlementInfo info : settlementMap.values()) {
            if (info.amountOwed.compareTo(BigDecimal.ZERO) > 0) {
                String name = userNames.getOrDefault(info.userId, "User " + info.userId);
                Label label = createSettlementLabel("Pay " + name + " " +
                        formatAmount(info.amountOwed));
                youOweContainer.getChildren().add(label);
                totalOwed = totalOwed.add(info.amountOwed);
            }
        }

        // Display "You're Owed" section
        for (SettlementInfo info : settlementMap.values()) {
            if (info.amountOwing.compareTo(BigDecimal.ZERO) > 0) {
                String name = userNames.getOrDefault(info.userId, "User " + info.userId);
                Label label = createSettlementLabel(name + " owes you " +
                        formatAmount(info.amountOwing));
                youAreOwedContainer.getChildren().add(label);
                totalOwing = totalOwing.add(info.amountOwing);
            }
        }

        if (youOweContainer.getChildren().isEmpty()) {
            youOweContainer.getChildren().add(createEmptyStateLabel("No pending dues."));
        }

        if (youAreOwedContainer.getChildren().isEmpty()) {
            youAreOwedContainer.getChildren().add(createEmptyStateLabel("Nobody owes you right now."));
        }

        // Add totals
        if (totalOwed.compareTo(BigDecimal.ZERO) > 0) {
            Label totalLabel = new Label("Total: " + formatAmount(totalOwed));
            totalLabel.getStyleClass().add("settlement-total");
            youOweContainer.getChildren().add(totalLabel);
        }

        if (totalOwing.compareTo(BigDecimal.ZERO) > 0) {
            Label totalLabel = new Label("Total: " + formatAmount(totalOwing));
            totalLabel.getStyleClass().add("settlement-total");
            youAreOwedContainer.getChildren().add(totalLabel);
        }

        // Display net balance
        BigDecimal netBalance = totalOwing.subtract(totalOwed);
        String netBalanceText;
        if (netBalance.compareTo(BigDecimal.ZERO) > 0) {
            netBalanceText = "Net balance: You are owed " + formatAmount(netBalance);
        } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
            netBalanceText = "Net balance: You owe " + formatAmount(netBalance.negate());
        } else {
            netBalanceText = "Net balance: Settled up!";
        }

        netBalanceLabel.setText(netBalanceText);
    }

    private void loadRecentExpenses(int userId) {
        ServiceResult<List<GroupModel>> groupsResult = groupsService.listGroupsForUser(userId, "");
        if (!groupsResult.success() || groupsResult.data() == null) {
            return;
        }

        List<ExpenseModel> allExpenses = new ArrayList<>();

        for (GroupModel group : groupsResult.data()) {
            ServiceResult<List<ExpenseModel>> expensesResult = expensesService.listExpensesForGroup(group.groupId());
            if (expensesResult.success() && expensesResult.data() != null) {
                allExpenses.addAll(expensesResult.data());
            }
        }

        // Sort by date descending and take last 5
        List<ExpenseModel> recent = allExpenses.stream()
                .sorted(Comparator.comparing(ExpenseModel::expenseDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        recentExpensesContainer.getChildren().clear();
        for (ExpenseModel expense : recent) {
            String payerName = getUserName(expense.payerId());

            BigDecimal myShare = null;
            ServiceResult<List<ExpenseSplitModel>> splitResult =
                    expensesService.listExpenseSplitsForExpense(expense.expenseId());
            if (splitResult.success() && splitResult.data() != null) {
                for (ExpenseSplitModel split : splitResult.data()) {
                    if (split.userId() == userId) {
                        myShare = split.shareAmount();
                        break;
                    }
                }
            }

            recentExpensesContainer.getChildren().add(createExpenseRow(expense, payerName, myShare));
        }

        if (recentExpensesContainer.getChildren().isEmpty()) {
            recentExpensesContainer.getChildren().add(createEmptyStateLabel("No recent expenses yet."));
        }
    }

    private void loadRecentTransactions(int userId) {
        ServiceResult<List<GroupModel>> groupsResult = groupsService.listGroupsForUser(userId, "");
        if (!groupsResult.success() || groupsResult.data() == null) {
            return;
        }

        List<TransactionModel> allTransactions = new ArrayList<>();

        for (GroupModel group : groupsResult.data()) {
            ServiceResult<List<TransactionModel>> transResult = transactionsService.listTransactionsForGroup(group.groupId());
            if (transResult.success() && transResult.data() != null) {
                allTransactions.addAll(transResult.data());
            }
        }

        // Filter to only include recent and unsettled, sort by date descending
        List<TransactionModel> recent = allTransactions.stream()
                .filter(t -> !t.settled())
                .sorted(Comparator.comparing(TransactionModel::transactionDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        recentTransactionsContainer.getChildren().clear();
        for (TransactionModel trans : recent) {
            String fromName = getUserName(trans.fromUserId());
            String toName = getUserName(trans.toUserId());
            recentTransactionsContainer.getChildren().add(
                    createRecentSettlementRow(fromName, toName, trans.amount(), trans.transactionDate().toString()));
        }

        if (recentTransactionsContainer.getChildren().isEmpty()) {
            recentTransactionsContainer.getChildren().add(createEmptyStateLabel("No recent settlements."));
        }
    }

    private Label createSettlementLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("settlement-item-label");
        label.setWrapText(true);
        return label;
    }

    private Label createEmptyStateLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("recent-list-empty");
        return label;
    }

    private VBox createExpenseRow(ExpenseModel expense, String payerName, BigDecimal myShare) {
        VBox row = new VBox(4);
        row.getStyleClass().add("recent-row");

        HBox topLine = new HBox(8);
        Label title = new Label(expense.title());
        title.getStyleClass().add("recent-row-title");

        Label totalAmount = new Label(formatAmount(expense.amount()));
        totalAmount.getStyleClass().add("recent-row-amount");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topLine.getChildren().addAll(title, spacer, totalAmount);

        Label subtitle = new Label("Paid by " + payerName + " on " + expense.expenseDate());
        subtitle.getStyleClass().add("recent-row-subtitle");

        String shareText = myShare == null
                ? "Your share: Not participating"
                : "Your share: " + formatAmount(myShare);
        Label shareLabel = new Label(shareText);
        shareLabel.getStyleClass().add(myShare == null ? "recent-row-share-muted" : "recent-row-share");

        row.getChildren().addAll(topLine, subtitle, shareLabel);
        return row;
    }

    private VBox createRecentSettlementRow(String fromName, String toName, BigDecimal amount, String date) {
        VBox row = new VBox(4);
        row.getStyleClass().add("recent-row");

        HBox topLine = new HBox(8);
        Label title = new Label(fromName + " → " + toName);
        title.getStyleClass().add("recent-row-title");

        Label amountLabel = new Label(formatAmount(amount));
        amountLabel.getStyleClass().add("recent-row-amount");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topLine.getChildren().addAll(title, spacer, amountLabel);

        Label subtitle = new Label("Settlement date: " + date);
        subtitle.getStyleClass().add("recent-row-subtitle");

        row.getChildren().addAll(topLine, subtitle);
        return row;
    }

    private String getUserName(int userId) {
        ServiceResult<UserAccount> userResult = userService.getProfile(userId);
        if (userResult.success() && userResult.data() != null) {
            return userResult.data().name();
        }
        return "User " + userId;
    }

    private String formatAmount(BigDecimal amount) {
        return "NPR " + String.format("%.2f", amount);
    }

    // Helper class to track settlement info for each person
    private static class SettlementInfo {
        int userId;
        BigDecimal amountOwed = BigDecimal.ZERO;
        BigDecimal amountOwing = BigDecimal.ZERO;

        SettlementInfo(int userId) {
            this.userId = userId;
        }

        void addOwed(BigDecimal amount) {
            this.amountOwed = this.amountOwed.add(amount);
        }

        void addOwing(BigDecimal amount) {
            this.amountOwing = this.amountOwing.add(amount);
        }
    }
}
