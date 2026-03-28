package com.splitms.controllers;

import com.splitms.models.GroupModel;
import com.splitms.services.ApplicationServices;
import com.splitms.services.CategoriesService;
import com.splitms.services.ExpensesService;
import com.splitms.services.GroupMembersService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import com.splitms.services.TransactionsService;
import com.splitms.utils.Validation;
import com.splitms.models.CategoryModel;
import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.models.GroupMemberView;
import com.splitms.models.TransactionModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GroupDetailsContentController {

    // ============ Services ============
    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final GroupMembersService groupMembersService = ApplicationServices.groupMembersService();
    private final CategoriesService categoriesService = ApplicationServices.categoriesService();
    private final ExpensesService expensesService = ApplicationServices.expensesService();
    private final TransactionsService transactionsService = ApplicationServices.transactionsService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    // ============ State ============
    private GroupModel group;
    private Runnable onBackRequest;
    private ObservableList<String> membersData = FXCollections.observableArrayList();
    private ObservableList<String> expensesData = FXCollections.observableArrayList();
    private ObservableList<String> settlementsData = FXCollections.observableArrayList();
    
    // ============ Cached Data for Dialogs ============
    private List<GroupMemberView> currentMembers = new ArrayList<>();
    private Map<Integer, GroupMemberView> membersByUserId = new HashMap<>();
    private ObservableList<CategoryModel> categoriesList = FXCollections.observableArrayList();

    // ============ FXML Injections ============
    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> membersListView;

    @FXML
    private ListView<String> expensesListView;

    @FXML
    private ListView<String> settlementsListView;

    @FXML
    private Button addMemberButton;

    @FXML
    private Button removeMemberButton;

    @FXML
    private Button addSettlementButton;

    public void setOnBackRequest(Runnable onBackRequest) {
        this.onBackRequest = onBackRequest;
    }

    public void loadGroup(GroupModel group) {
        this.group = group;
        if (group == null) {
            return;
        }

        titleLabel.setText(group.groupName());
        subtitleLabel.setText(group.description());
        
        // Initialize lists
        membersListView.setItems(membersData);
        expensesListView.setItems(expensesData);
        settlementsListView.setItems(settlementsData);
        
        // Hide member and settlement buttons for personal default groups
        boolean isPersonalGroup = group.personalDefault();
        addMemberButton.setVisible(!isPersonalGroup);
        removeMemberButton.setVisible(!isPersonalGroup);
        addSettlementButton.setVisible(!isPersonalGroup);
        
        // Load cached data for dialogs
        loadMembersForDialogs();
        loadCategoriesForDialogs();
        
        // Load display data
        refreshGroupData();
    }

    /**
     * Loads all group members into the currentMembers cache.
     * Used by dialogs to populate combo boxes and checkboxes.
     */
    private void loadMembersForDialogs() {
        currentMembers.clear();
        membersByUserId.clear();
        
        if (group == null) return;
        
        ServiceResult<List<GroupMemberView>> result = groupMembersService.listMembers(group.groupId());
        if (result.success() && result.data() != null) {
            currentMembers.addAll(result.data());
            for (GroupMemberView member : result.data()) {
                membersByUserId.put(member.userId(), member);
            }
        }
    }

    /**
     * Loads all categories from the service into the categoriesList cache.
     * Used by expense dialog to populate category dropdown.
     */
    private void loadCategoriesForDialogs() {
        categoriesList.clear();
        
        ServiceResult<List<CategoryModel>> result = categoriesService.listCategories();
        if (result.success() && result.data() != null) {
            categoriesList.addAll(result.data());
        }
    }

    /**
     * Refreshes all group-related data (members, expenses, settlements).
     * Called after adding/removing members or expenses.
     */
    private void refreshGroupData() {
        if (group == null) return;
        
        loadGroupMembers();
        loadGroupExpenses();
        loadGroupSettlements();
    }

    @FXML
    private void onBack() {
        if (onBackRequest != null) {
            onBackRequest.run();
        }
    }

    @FXML
    private void onEditGroup() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        Optional<GroupInput> input = showGroupDialog("Edit Group", group.groupName(), group.description());
        if (input.isEmpty()) {
            return;
        }

        ServiceResult<GroupModel> result = groupsService.updateGroup(
                group.groupId(),
                sessionManager.getUserId(),
                input.get().name(),
                input.get().description());

        if (!result.success() || result.data() == null) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        loadGroup(result.data());
    }

    @FXML
    private void onDeleteGroup() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        // Check if it's a personal default group
        if (group.personalDefault()) {
            showError("Personal default groups cannot be deleted. You can only edit them.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Group");
        confirm.setHeaderText("Delete " + group.groupName() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> confirmResult = confirm.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        ServiceResult<Void> result = groupsService.deleteGroup(group.groupId(), sessionManager.getUserId());
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        onBack();
    }

    // ============ ADD MEMBER ============

    /**
     * Opens a dialog for adding a new member to the group via email.
     * Validates email format and prevents duplicate member additions.
     */
    @FXML
    private void onAddMember() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        Optional<MemberInput> input = showAddMemberDialog();
        if (input.isEmpty()) {
            return;
        }

        String email = input.get().email().trim();
        
        // Validate email format
        if (!Validation.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        ServiceResult<Void> result = groupMembersService.addMemberByEmail(group.groupId(), email);
        
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess("Member added successfully!");
        refreshGroupData();
    }

    /**
     * Shows a dialog for entering member email.
     * 
     * @return Optional containing MemberInput record with email, or empty if cancelled.
     */
    private Optional<MemberInput> showAddMemberDialog() {
        Dialog<MemberInput> dialog = new Dialog<>();
        dialog.setTitle("Add Group Member");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter member email");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Email"), 0, 0);
        grid.add(emailField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new MemberInput(emailField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ============ REMOVE MEMBERS ============

    /**
     * Opens a dialog for removing members from the group.
     * Shows a checklist of actual group members (excludes group owner).
     * Allows multi-select for batch removal.
     */
    @FXML
    private void onRemoveMembers() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        Optional<List<Integer>> selectedUserIds = showRemoveMembersDialog();
        if (selectedUserIds.isEmpty() || selectedUserIds.get().isEmpty()) {
            return;
        }

        // Remove each selected member
        for (Integer userId : selectedUserIds.get()) {
            ServiceResult<Void> result = groupMembersService.removeMember(group.groupId(), userId);
            if (!result.success()) {
                showError("Failed to remove member: " + result.message());
                return;
            }
        }
        
        showSuccess("Member(s) removed successfully!");
        refreshGroupData();
    }

    /**
     * Shows a dialog with checkboxes for selecting members to remove from the group.
     * Displays all current group members except the group owner.
     * Allows multi-select for batch removal.
     * 
     * @return Optional containing list of selected user IDs.
     */
    private Optional<List<Integer>> showRemoveMembersDialog() {
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Remove Group Members");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ScrollPane scrollPane = new ScrollPane();
        VBox membersList = new VBox(8);
        membersList.setPadding(new Insets(10));
        scrollPane.setContent(membersList);
        scrollPane.setFitToWidth(true);
        
        List<Integer> selectedUserIds = new ArrayList<>();
        
        Label warningLabel = new Label("⚠ Removing a member will not delete their expense history.");
        warningLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11;");
        membersList.getChildren().add(warningLabel);
        
        // Add checkboxes for each member
        for (GroupMemberView member : currentMembers) {
            // Exclude group owner from removal
            if (member.userId() == group.groupId()) {
                continue; // Skip owner
            }
            
            CheckBox checkBox = new CheckBox(member.name() + " (" + member.email() + ")");
            int memberId = member.userId();
            
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedUserIds.add(memberId);
                } else {
                    selectedUserIds.remove(Integer.valueOf(memberId));
                }
            });
            
            membersList.getChildren().add(checkBox);
        }

        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return selectedUserIds;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ============ ADD EXPENSE ============

    /**
     * Opens a dialog for adding a new expense to the group.
     * Supports equal or custom split types.
     */
    @FXML
    private void onAddExpense() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        Optional<ExpenseInput> input = showAddExpenseDialog();
        if (input.isEmpty()) {
            return;
        }

        // Validate amount
        if (input.get().amount() == null || input.get().amount().compareTo(BigDecimal.ZERO) <= 0) {
            showError("Amount must be greater than 0.");
            return;
        }

        // Validate title
        if (input.get().title() == null || input.get().title().isBlank()) {
            showError("Title is required.");
            return;
        }

        // Get payer user ID from selected member name
        GroupMemberView selectedPayer = null;
        for (GroupMemberView member : currentMembers) {
            if ((member.name() + " (" + member.email() + ")").equals(input.get().payer())) {
                selectedPayer = member;
                break;
            }
        }
        
        if (selectedPayer == null) {
            showError("Invalid payer selected.");
            return;
        }

        // Get category ID from selected category
        CategoryModel selectedCategory = null;
        for (CategoryModel cat : categoriesList) {
            if (cat.categoryName().equals(input.get().category())) {
                selectedCategory = cat;
                break;
            }
        }
        
        if (selectedCategory == null) {
            showError("Invalid category selected.");
            return;
        }

        // Build expense splits based on split type
        Set<GroupMemberView> selectedMembers = input.get().selectedMembers();
        
        // Validate that at least one member is selected
        if (selectedMembers == null || selectedMembers.isEmpty()) {
            showError("At least one member must be selected for the expense split.");
            return;
        }
        
        // Ensure payer is always included in the split
        selectedMembers.add(selectedPayer);
        
        // For CUSTOM split, show custom percentage dialog
        Map<GroupMemberView, Float> customPercentages = input.get().customPercentages();
        if ("CUSTOM".equals(input.get().splitType())) {
            Optional<Map<GroupMemberView, Float>> customPercentagesOpt = showCustomPercentageSplitDialog(selectedMembers);
            if (customPercentagesOpt.isEmpty()) {
                // User cancelled the custom percentage dialog
                return;
            }
            customPercentages = customPercentagesOpt.get();
        }
        
        List<ExpenseSplitModel> splits = new ArrayList<>();
        
        if ("EQUAL".equals(input.get().splitType())) {
            // Equal split among selected members
            BigDecimal shareAmount = input.get().amount().divide(
                BigDecimal.valueOf(selectedMembers.size()),
                2,
                java.math.RoundingMode.HALF_UP
            );
            float sharePercentage = 100f / selectedMembers.size();
            
            for (GroupMemberView member : selectedMembers) {
                splits.add(new ExpenseSplitModel(
                    0, // splitId (not set yet)
                    0, // expenseId (not set yet)
                    member.userId(),
                    shareAmount,
                    sharePercentage
                ));
            }
        } else {
            // Custom split - use percentages provided by user
            for (GroupMemberView member : selectedMembers) {
                Float percentage = customPercentages.getOrDefault(member, 0f);
                BigDecimal shareAmount = input.get().amount()
                    .multiply(BigDecimal.valueOf(percentage / 100.0))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                
                splits.add(new ExpenseSplitModel(
                    0, // splitId (not set yet)
                    0, // expenseId (not set yet)
                    member.userId(),
                    shareAmount,
                    percentage
                ));
            }
        }

        // Create expense with splits through service
        ServiceResult<ExpenseModel> result = expensesService.createExpenseWithSplits(
            group.groupId(),
            selectedPayer.userId(),
            selectedCategory.categoryId(),
            input.get().amount(),
            input.get().date(),
            input.get().title(),
            input.get().description(),
            splits
        );
        
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess("Expense added successfully!");
        refreshGroupData();
    }

    /**
     * Shows a dialog for entering expense details.
     * Supports equal split and custom percentage split types.
     * Uses real member and category data from services.
     * 
     * @return Optional containing ExpenseInput record with all expense details.
     */
    private Optional<ExpenseInput> showAddExpenseDialog() {
        Dialog<ExpenseInput> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        
        TextField titleField = new TextField();
        titleField.setPromptText("e.g. Lunch, Gas, Movies");
        
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Optional description");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        // Load payers from real members
        ObservableList<String> payerNames = FXCollections.observableArrayList();
        for (GroupMemberView member : currentMembers) {
            payerNames.add(member.name() + " (" + member.email() + ")");
        }
        
        ComboBox<String> payerCombo = new ComboBox<>(payerNames);
        // Set default payer to logged-in user if they're in the group
        if (!payerNames.isEmpty()) {
            String loggedInUserName = sessionManager.getUserName();
            String loggedInUserEmail = sessionManager.getUserEmail();
            String loggedInUserDisplay = loggedInUserName + " (" + loggedInUserEmail + ")";
            
            int loggedInUserIndex = payerNames.indexOf(loggedInUserDisplay);
            if (loggedInUserIndex >= 0) {
                payerCombo.getSelectionModel().select(loggedInUserIndex);
            } else {
                payerCombo.getSelectionModel().selectFirst();
            }
        }
        
        // Load categories from real data
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        for (CategoryModel cat : categoriesList) {
            categoryNames.add(cat.categoryName());
        }
        
        ComboBox<String> categoryCombo = new ComboBox<>(categoryNames);
        if (!categoryNames.isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        
        ToggleGroup splitGroup = new ToggleGroup();
        RadioButton equalSplit = new RadioButton("Equal Split");
        RadioButton customSplit = new RadioButton("Custom Percentage");
        equalSplit.setToggleGroup(splitGroup);
        customSplit.setToggleGroup(splitGroup);
        equalSplit.setSelected(true);
        
        // Create scrollable member selection section
        ScrollPane memberScrollPane = new ScrollPane();
        VBox memberCheckboxes = new VBox(5);
        memberCheckboxes.setPadding(new Insets(5));
        memberScrollPane.setContent(memberCheckboxes);
        memberScrollPane.setFitToWidth(true);
        memberScrollPane.setPrefHeight(150);
        
        Set<GroupMemberView> selectedMembers = new HashSet<>();

        grid.add(new Label("Amount *"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Title *"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Description"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Date *"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Category *"), 0, 4);
        grid.add(categoryCombo, 1, 4);
        grid.add(new Label("Payer *"), 0, 5);
        grid.add(payerCombo, 1, 5);
        grid.add(new Label("Split Type"), 0, 6);
        grid.add(equalSplit, 1, 6);
        grid.add(customSplit, 1, 7);
        grid.add(new Label("Members *"), 0, 8);
        grid.add(memberScrollPane, 1, 8);
        
        // Populate member checkboxes and set defaults
        GroupMemberView payerMember = null;
        for (GroupMemberView member : currentMembers) {
            CheckBox memberCheckBox = new CheckBox(member.name() + " (" + member.email() + ")");
            memberCheckBox.setSelected(true);
            selectedMembers.add(member);
            
            final GroupMemberView currentMember = member;
            memberCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedMembers.add(currentMember);
                } else {
                    selectedMembers.remove(currentMember);
                }
            });
            
            memberCheckboxes.getChildren().add(memberCheckBox);
        }
        
        // Update payer checkbox when payer selection changes
        payerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            
            // Find the new payer member
            GroupMemberView newPayerMember = null;
            for (GroupMemberView member : currentMembers) {
                if ((member.name() + " (" + member.email() + ")").equals(newVal)) {
                    newPayerMember = member;
                    break;
                }
            }
            
            // Reset all checkboxes state
            for (var node : memberCheckboxes.getChildren()) {
                if (node instanceof CheckBox cb) {
                    cb.setDisable(false);
                }
            }
            
            // Disable and force-select payer checkbox
            if (newPayerMember != null) {
                final GroupMemberView finalPayerMember = newPayerMember;
                for (var node : memberCheckboxes.getChildren()) {
                    if (node instanceof CheckBox cb) {
                        if (cb.getText().equals(finalPayerMember.name() + " (" + finalPayerMember.email() + ")")) {
                            cb.setDisable(true);
                            cb.setSelected(true);
                            selectedMembers.add(finalPayerMember);
                            break;
                        }
                    }
                }
            }
        });
        
        // Set initial payer checkbox as disabled
        String initialPayer = payerCombo.getValue();
        if (initialPayer != null) {
            for (GroupMemberView member : currentMembers) {
                if ((member.name() + " (" + member.email() + ")").equals(initialPayer)) {
                    payerMember = member;
                    break;
                }
            }
            
            if (payerMember != null) {
                final GroupMemberView finalPayerMember = payerMember;
                for (var node : memberCheckboxes.getChildren()) {
                    if (node instanceof CheckBox cb) {
                        if (cb.getText().equals(finalPayerMember.name() + " (" + finalPayerMember.email() + ")")) {
                            cb.setDisable(true);
                            break;
                        }
                    }
                }
            }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    return new ExpenseInput(
                        amount,
                        titleField.getText(),
                        descriptionField.getText(),
                        datePicker.getValue(),
                        categoryCombo.getValue(),
                        payerCombo.getValue(),
                        equalSplit.isSelected() ? "EQUAL" : "CUSTOM",
                        new HashSet<>(selectedMembers),
                        new HashMap<>()
                    );
                } catch (NumberFormatException e) {
                    showError("Invalid amount format. Please enter a valid number.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Shows a dialog for setting custom percentage splits for each member.
     * Validates that percentages sum to 100%.
     * 
     * @param selectedMembers set of selected members
     * @return Optional containing map of members to their custom percentages
     */
    private Optional<Map<GroupMemberView, Float>> showCustomPercentageSplitDialog(Set<GroupMemberView> selectedMembers) {
        Dialog<Map<GroupMemberView, Float>> dialog = new Dialog<>();
        dialog.setTitle("Custom Percentage Split");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(8);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        Label intro = new Label("Set the percentage each member owes (must total 100%):");
        intro.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(intro);

        // Store percentage spinners
        Map<GroupMemberView, javafx.scene.control.Spinner<Double>> spinners = new HashMap<>();
        Map<GroupMemberView, Float> customPercentages = new HashMap<>();

        // Create percentage input for each member
        double equalPercentage = 100.0 / selectedMembers.size();
        for (GroupMemberView member : selectedMembers) {
            javafx.scene.layout.HBox memberRow = new javafx.scene.layout.HBox(10);
            memberRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            memberRow.setPadding(new Insets(5, 0, 5, 0));

            Label memberLabel = new Label(member.name() + " (" + member.email() + ")");
            memberLabel.setMinWidth(200);

            javafx.scene.control.Spinner<Double> percentSpinner = 
                new javafx.scene.control.Spinner<>(0.0, 100.0, equalPercentage, 0.1);
            percentSpinner.setPrefWidth(100);
            percentSpinner.setEditable(true);

            Label percentLabel = new Label("%");

            memberRow.getChildren().addAll(memberLabel, percentSpinner, percentLabel);
            content.getChildren().add(memberRow);
            spinners.put(member, percentSpinner);
            customPercentages.put(member, (float) equalPercentage);
        }

        // Add total calculator
        Label totalLabel = new Label("Total: " + String.format("%.1f", 100.0) + "%");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        content.getChildren().add(new javafx.scene.control.Separator());
        content.getChildren().add(totalLabel);

        // Update total when percentages change
        for (var entry : spinners.entrySet()) {
            entry.getValue().valueProperty().addListener((obs, oldVal, newVal) -> {
                double total = spinners.values().stream()
                        .mapToDouble(spinner -> spinner.getValue() != null ? spinner.getValue() : 0.0)
                        .sum();
                totalLabel.setText("Total: " + String.format("%.1f", total) + "%");
                
                // Update map
                for (var memberSpinner : spinners.entrySet()) {
                    Double value = memberSpinner.getValue().getValue();
                    customPercentages.put(memberSpinner.getKey(), value != null ? value.floatValue() : 0f);
                }
            });
        }

        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validate that percentages sum to 100%
                double total = customPercentages.values().stream()
                        .mapToDouble(Float::doubleValue)
                        .sum();

                if (Math.abs(total - 100.0) > 0.01) {
                    showError("Percentages must sum to 100%, current total: " + String.format("%.1f", total) + "%");
                    return null;
                }

                return new HashMap<>(customPercentages);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ============ ADD SETTLEMENT ============

    /**
     * Opens a dialog for recording a settlement/payment between group members.
     * Creates a transaction record marked as settled.
     */
    @FXML
    private void onAddSettlement() {
        if (group == null) {
            showError("Group not loaded.");
            return;
        }

        Optional<SettlementInput> input = showAddSettlementDialog();
        if (input.isEmpty()) {
            return;
        }

        // Validate amount
        if (input.get().amount() == null || input.get().amount().compareTo(BigDecimal.ZERO) <= 0) {
            showError("Settlement amount must be greater than 0.");
            return;
        }

        // Get from and to user IDs from names
        GroupMemberView fromMember = null;
        GroupMemberView toMember = null;
        
        for (GroupMemberView member : currentMembers) {
            String displayName = member.name() + " (" + member.email() + ")";
            if (displayName.equals(input.get().fromMember())) {
                fromMember = member;
            }
            if (displayName.equals(input.get().toMember())) {
                toMember = member;
            }
        }
        
        if (fromMember == null || toMember == null) {
            showError("Invalid member selection for settlement.");
            return;
        }
        
        if (fromMember.userId() == toMember.userId()) {
            showError("Cannot settle payment to the same person.");
            return;
        }

        // Create settlement transaction through service
        ServiceResult<TransactionModel> result = transactionsService.createTransaction(
            group.groupId(),
            fromMember.userId(),
            toMember.userId(),
            input.get().amount(),
            input.get().date()
        );
        
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess("Settlement recorded successfully!");
        refreshGroupData();
    }

    /**
     * Shows a dialog for entering settlement details.
     * Allows selection of from/to members from actual group members.
     * Prevents selecting the same person twice.
     * 
     * @return Optional containing SettlementInput record with settlement details.
     */
    private Optional<SettlementInput> showAddSettlementDialog() {
        Dialog<SettlementInput> dialog = new Dialog<>();
        dialog.setTitle("Record Settlement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        // Build member name list
        ObservableList<String> memberNames = FXCollections.observableArrayList();
        for (GroupMemberView member : currentMembers) {
            memberNames.add(member.name() + " (" + member.email() + ")");
        }
        
        ComboBox<String> fromCombo = new ComboBox<>(memberNames);
        ComboBox<String> toCombo = new ComboBox<>(memberNames);
        
        if (!memberNames.isEmpty()) {
            fromCombo.getSelectionModel().selectFirst();
            // Select different member for 'to' if possible
            if (memberNames.size() > 1) {
                toCombo.getSelectionModel().selectLast();
            } else {
                toCombo.getSelectionModel().selectFirst();
            }
        }
        
        // Add listener to prevent same person in both fields
        fromCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(toCombo.getValue())) {
                // Auto-select a different member for 'to'
                for (String member : memberNames) {
                    if (!member.equals(newVal)) {
                        toCombo.setValue(member);
                        break;
                    }
                }
            }
        });
        
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("From *"), 0, 0);
        grid.add(fromCombo, 1, 0);
        grid.add(new Label("To *"), 0, 1);
        grid.add(toCombo, 1, 1);
        grid.add(new Label("Amount *"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Date *"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String fromVal = fromCombo.getValue();
                    String toVal = toCombo.getValue();
                    
                    if (fromVal == null || toVal == null) {
                        showError("Please select both members.");
                        return null;
                    }
                    
                    if (fromVal.equals(toVal)) {
                        showError("Cannot settle payment to the same person.");
                        return null;
                    }
                    
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    return new SettlementInput(
                        fromVal,
                        toVal,
                        amount,
                        datePicker.getValue()
                    );
                } catch (NumberFormatException e) {
                    showError("Invalid amount format. Please enter a valid number.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ============ DATA LOADING HELPERS ============

    /**
     * Loads group members from the service and updates the members list view.
     * Fetches all members of the current group and displays them in the list view.
     */
    private void loadGroupMembers() {
        if (group == null) return;
        
        ServiceResult<List<GroupMemberView>> result = groupMembersService.listMembers(group.groupId());
        membersData.clear();
        
        if (result.success() && result.data() != null) {
            for (GroupMemberView member : result.data()) {
                membersData.add(member.name() + " (" + member.email() + ")");
            }
        }
    }

    /**
     * Loads recent expenses from the service and updates the expenses list view.
     * Displays formatted expense data: "Title - $amount (paid by Payer) - Date"
     */
    private void loadGroupExpenses() {
        if (group == null) return;
        
        expensesData.clear();
        
        ServiceResult<List<ExpenseModel>> result = expensesService.listExpensesForGroup(group.groupId());
        if (result.success() && result.data() != null) {
            for (ExpenseModel expense : result.data()) {
                String payerName = membersByUserId.containsKey(expense.payerId()) 
                    ? membersByUserId.get(expense.payerId()).name()
                    : "Unknown";
                
                String displayText = String.format("%s - $%s (paid by %s) - %s",
                    expense.title(),
                    expense.amount(),
                    payerName,
                    expense.expenseDate()
                );
                expensesData.add(displayText);
            }
        }
    }

    /**
     * Loads settlement transactions from the service and updates the settlements list view.
     * Displays formatted settlement data: "From → To: $amount - Date"
     */
    private void loadGroupSettlements() {
        if (group == null) return;
        
        settlementsData.clear();
        
        ServiceResult<List<TransactionModel>> result = transactionsService.listTransactionsForGroup(group.groupId());
        if (result.success() && result.data() != null) {
            for (TransactionModel transaction : result.data()) {
                String fromName = membersByUserId.containsKey(transaction.fromUserId())
                    ? membersByUserId.get(transaction.fromUserId()).name()
                    : "Unknown";
                
                String toName = membersByUserId.containsKey(transaction.toUserId())
                    ? membersByUserId.get(transaction.toUserId()).name()
                    : "Unknown";
                
                String displayText = String.format("%s → %s: $%s - %s",
                    fromName,
                    toName,
                    transaction.amount(),
                    transaction.transactionDate()
                );
                settlementsData.add(displayText);
            }
        }
    }

    /**
     * Dialog factory for editing group information.
     * 
     * @param title the dialog title
     * @param initialName the initial group name
     * @param initialDescription the initial group description
     * @return Optional containing GroupInput record with edited values
     */
    private Optional<GroupInput> showGroupDialog(String title, String initialName, String initialDescription) {
        Dialog<GroupInput> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(initialName == null ? "" : initialName);
        TextField descriptionField = new TextField(initialDescription == null ? "" : initialDescription);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new GroupInput(nameField.getText(), descriptionField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showError(String message) {
        statusLabel.getStyleClass().remove("status-success");
        if (!statusLabel.getStyleClass().contains("status-error")) {
            statusLabel.getStyleClass().add("status-error");
        }
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.getStyleClass().remove("status-error");
        if (!statusLabel.getStyleClass().contains("status-success")) {
            statusLabel.getStyleClass().add("status-success");
        }
        statusLabel.setText(message);
    }

    // ============ INNER RECORD CLASSES ============

    /**
     * Record for group edit dialog input.
     * 
     * @param name the group name
     * @param description the group description
     */
    private record GroupInput(String name, String description) {
    }

    /**
     * Record for member addition input.
     * 
     * @param email the email of the member to add
     */
    private record MemberInput(String email) {
    }

    /**
     * Record for expense input.
     * 
     * @param amount the expense amount
     * @param description optional description
     * @param date the expense date
     * @param payer the member who paid
     * @param splitType "EQUAL" or "CUSTOM" split type
     * @param selectedMembers set of group members selected for this expense split
     * @param customPercentages map of members to their custom percentage shares (for CUSTOM split type)
     */
    private record ExpenseInput(
        BigDecimal amount,
        String title,
        String description,
        LocalDate date,
        String category,
        String payer,
        String splitType,
        Set<GroupMemberView> selectedMembers,
        Map<GroupMemberView, Float> customPercentages
    ) {
    }

    /**
     * Record for settlement input.
     * 
     * @param fromMember the member paying/settling
     * @param toMember the member receiving payment
     * @param amount the settlement amount
     * @param date the settlement date
     */
    private record SettlementInput(
        String fromMember,
        String toMember,
        BigDecimal amount,
        LocalDate date
    ) {
    }
}
