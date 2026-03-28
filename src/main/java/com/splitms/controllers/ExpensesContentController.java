package com.splitms.controllers;

import com.splitms.models.CategoryModel;
import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.models.GroupModel;
import com.splitms.models.UserAccount;
import com.splitms.services.ApplicationServices;
import com.splitms.services.CategoriesService;
import com.splitms.services.ExpensesService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import com.splitms.services.UserService;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ExpensesContentController implements Initializable {

    @FXML
    private Label totalExpensesValueLabel;

    @FXML
    private Label yourShareValueLabel;

    @FXML
    private Label thisMonthValueLabel;

    @FXML
    private Label expenseCountValueLabel;

    @FXML
    private Label categoryChartSummaryLabel;

    @FXML
    private Label groupChartSummaryLabel;

    @FXML
    private Label recentSummaryLabel;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private BarChart<String, Number> groupBarChart;

    @FXML
    private VBox recentExpensesContainer;

    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final ExpensesService expensesService = ApplicationServices.expensesService();
    private final CategoriesService categoriesService = ApplicationServices.categoriesService();
    private final UserService userService = ApplicationServices.userService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadExpensesAnalytics();
    }

    private void loadExpensesAnalytics() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            setDefaultValues();
            showEmptyRecent("Please log in to view expenses.");
            return;
        }

        ServiceResult<List<GroupModel>> groupsResult = groupsService.listGroupsForUser(userId, "");
        if (!groupsResult.success() || groupsResult.data() == null) {
            setDefaultValues();
            showEmptyRecent("Unable to load expenses right now.");
            return;
        }

        List<GroupModel> groups = groupsResult.data();
        Map<Integer, String> groupNameById = groups.stream()
                .collect(Collectors.toMap(GroupModel::groupId, GroupModel::groupName, (a, b) -> a));

        Map<Integer, String> categoryNameById = loadCategoryNameMap();
        List<ExpenseWithShare> allExpensesWithShare = collectExpensesWithShare(groups, userId);

        if (allExpensesWithShare.isEmpty()) {
            setDefaultValues();
            showEmptyRecent("No expenses recorded yet.");
            categoryChartSummaryLabel.setText("No category data");
            groupChartSummaryLabel.setText("No group data");
            recentSummaryLabel.setText("0 records");
            return;
        }

        populateMetrics(allExpensesWithShare);
        populateCategoryChart(allExpensesWithShare, categoryNameById);
        populateGroupChart(allExpensesWithShare, groupNameById);
        populateRecentExpenses(allExpensesWithShare, groupNameById, categoryNameById, userId);
    }

    private List<ExpenseWithShare> collectExpensesWithShare(List<GroupModel> groups, int currentUserId) {
        List<ExpenseWithShare> allExpenses = new ArrayList<>();

        for (GroupModel group : groups) {
            ServiceResult<List<ExpenseModel>> expensesResult = expensesService.listExpensesForGroup(group.groupId());
            if (!expensesResult.success() || expensesResult.data() == null) {
                continue;
            }

            for (ExpenseModel expense : expensesResult.data()) {
                BigDecimal myShare = null;
                ServiceResult<List<ExpenseSplitModel>> splitResult =
                        expensesService.listExpenseSplitsForExpense(expense.expenseId());

                if (splitResult.success() && splitResult.data() != null) {
                    for (ExpenseSplitModel split : splitResult.data()) {
                        if (split.userId() == currentUserId) {
                            myShare = split.shareAmount();
                            break;
                        }
                    }
                }

                allExpenses.add(new ExpenseWithShare(expense, myShare));
            }
        }

        return allExpenses;
    }

    private Map<Integer, String> loadCategoryNameMap() {
        Map<Integer, String> categoryMap = new HashMap<>();

        ServiceResult<List<CategoryModel>> categoriesResult = categoriesService.listCategories();
        if (!categoriesResult.success() || categoriesResult.data() == null) {
            return categoryMap;
        }

        for (CategoryModel category : categoriesResult.data()) {
            categoryMap.put(category.categoryId(), category.categoryName());
        }

        return categoryMap;
    }

    private void populateMetrics(List<ExpenseWithShare> expensesWithShare) {
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal yourShare = BigDecimal.ZERO;
        BigDecimal thisMonth = BigDecimal.ZERO;
        YearMonth currentMonth = YearMonth.from(LocalDate.now());

        for (ExpenseWithShare item : expensesWithShare) {
            totalExpenses = totalExpenses.add(item.expense().amount());

            if (item.myShare() != null) {
                yourShare = yourShare.add(item.myShare());
            }

            if (YearMonth.from(item.expense().expenseDate()).equals(currentMonth)) {
                thisMonth = thisMonth.add(item.expense().amount());
            }
        }

        totalExpensesValueLabel.setText(formatAmount(totalExpenses));
        yourShareValueLabel.setText(formatAmount(yourShare));
        thisMonthValueLabel.setText(formatAmount(thisMonth));
        expenseCountValueLabel.setText(String.valueOf(expensesWithShare.size()));
    }

    private void populateCategoryChart(List<ExpenseWithShare> expensesWithShare, Map<Integer, String> categoryNameById) {
        Map<String, BigDecimal> totalsByCategory = new LinkedHashMap<>();

        for (ExpenseWithShare item : expensesWithShare) {
            String categoryName = categoryNameById.getOrDefault(
                    item.expense().categoryId(),
                    "Category " + item.expense().categoryId());
            totalsByCategory.merge(categoryName, item.expense().amount(), BigDecimal::add);
        }

        List<PieChart.Data> chartData = totalsByCategory.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()))
                .collect(Collectors.toList());

        categoryPieChart.setData(FXCollections.observableArrayList(chartData));
        categoryPieChart.setClockwise(true);

        BigDecimal total = totalsByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        categoryChartSummaryLabel.setText("Total: " + formatAmount(total));
    }

    private void populateGroupChart(List<ExpenseWithShare> expensesWithShare, Map<Integer, String> groupNameById) {
        Map<String, BigDecimal> totalsByGroup = new LinkedHashMap<>();

        for (ExpenseWithShare item : expensesWithShare) {
            String groupName = groupNameById.getOrDefault(item.expense().groupId(), "Group " + item.expense().groupId());
            totalsByGroup.merge(groupName, item.expense().amount(), BigDecimal::add);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, BigDecimal> entry : totalsByGroup.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue()));
        }

        groupBarChart.getData().clear();
        groupBarChart.getData().add(series);

        BigDecimal total = totalsByGroup.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        groupChartSummaryLabel.setText("Total: " + formatAmount(total));
    }

    private void populateRecentExpenses(List<ExpenseWithShare> expensesWithShare,
                                        Map<Integer, String> groupNameById,
                                        Map<Integer, String> categoryNameById,
                                        int currentUserId) {
        recentExpensesContainer.getChildren().clear();

        List<ExpenseWithShare> recent = expensesWithShare.stream()
                .sorted(Comparator.comparing(item -> item.expense().expenseDate(), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());

        for (ExpenseWithShare item : recent) {
            ExpenseModel expense = item.expense();
            String payerName = getUserName(expense.payerId());
            String groupName = groupNameById.getOrDefault(expense.groupId(), "Group " + expense.groupId());
            String categoryName = categoryNameById.getOrDefault(expense.categoryId(), "Category " + expense.categoryId());
            recentExpensesContainer.getChildren().add(
                    createRecentExpenseRow(expense, payerName, groupName, categoryName, item.myShare(), currentUserId));
        }

        if (recentExpensesContainer.getChildren().isEmpty()) {
            showEmptyRecent("No recent expenses found.");
        }

        recentSummaryLabel.setText(recent.size() + " latest records");
    }

    private VBox createRecentExpenseRow(ExpenseModel expense,
                                        String payerName,
                                        String groupName,
                                        String categoryName,
                                        BigDecimal myShare,
                                        int currentUserId) {
        VBox row = new VBox(4);
        row.getStyleClass().add("recent-row");

        HBox topLine = new HBox(8);
        Label titleLabel = new Label(expense.title());
        titleLabel.getStyleClass().add("recent-row-title");

        Label amountLabel = new Label(formatAmount(expense.amount()));
        amountLabel.getStyleClass().add("recent-row-amount");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topLine.getChildren().addAll(titleLabel, spacer, amountLabel);

        Label subtitleLabel = new Label(
                "Paid by " + payerName + " on " + expense.expenseDate() + " • "
                        + groupName + " • " + categoryName);
        subtitleLabel.getStyleClass().add("recent-row-subtitle");

        String shareText;
        if (expense.payerId() == currentUserId) {
            shareText = "You paid this expense.";
        } else if (myShare != null) {
            shareText = "Your share: " + formatAmount(myShare);
        } else {
            shareText = "Your share: Not participating";
        }

        Label shareLabel = new Label(shareText);
        shareLabel.getStyleClass().add(myShare == null && expense.payerId() != currentUserId
                ? "recent-row-share-muted"
                : "recent-row-share");

        row.getChildren().addAll(topLine, subtitleLabel, shareLabel);
        return row;
    }

    private String getUserName(int userId) {
        ServiceResult<UserAccount> userResult = userService.getProfile(userId);
        if (userResult.success() && userResult.data() != null) {
            return userResult.data().name();
        }
        return "User " + userId;
    }

    private void setDefaultValues() {
        totalExpensesValueLabel.setText("NPR 0.00");
        yourShareValueLabel.setText("NPR 0.00");
        thisMonthValueLabel.setText("NPR 0.00");
        expenseCountValueLabel.setText("0");
        categoryPieChart.setData(FXCollections.observableArrayList());
        groupBarChart.getData().clear();
    }

    private void showEmptyRecent(String text) {
        recentExpensesContainer.getChildren().clear();
        Label emptyLabel = new Label(text);
        emptyLabel.getStyleClass().add("recent-list-empty");
        recentExpensesContainer.getChildren().add(emptyLabel);
    }

    private String formatAmount(BigDecimal amount) {
        return "NPR " + String.format("%.2f", amount);
    }

    private record ExpenseWithShare(ExpenseModel expense, BigDecimal myShare) {
    }
}
