package com.splitms.controllers;

import com.splitms.models.GroupModel;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ExpensesService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GroupsContentController {

    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final ExpensesService expensesService = new ExpensesService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final ObservableList<GroupModel> groups = FXCollections.observableArrayList();
    private final Map<Integer, Integer> expenseCountByGroup = new HashMap<>();

    private GroupModel selectedGroup;
    private Consumer<GroupModel> onGroupOpenRequest;

    @FXML
    private Label statusLabel;

    @FXML
    private FlowPane groupsCardsContainer;

    @FXML
    private void initialize() {
        groupsCardsContainer.widthProperty().addListener((obs, oldValue, newValue) -> {
            groupsCardsContainer.setPrefWrapLength(Math.max(640.0, newValue.doubleValue() - 12.0));
        });

        loadGroups();
    }

    @FXML
    private void onCreateGroup() {
        Optional<GroupInput> input = showGroupDialog("Create Group", null, null);
        if (input.isEmpty()) {
            return;
        }

        ServiceResult<GroupModel> result = groupsService.createGroup(
                sessionManager.getUserId(),
                input.get().name(),
                input.get().description());

        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        loadGroups();
        if (result.data() != null) {
            selectGroupById(result.data().groupId());
        }
    }

    private void loadGroups() {
        ServiceResult<List<GroupModel>> result = groupsService.listGroupsForUser(
                sessionManager.getUserId(),
                null);

        groups.setAll(result.data() == null ? List.of() : result.data());
        expenseCountByGroup.clear();
        renderGroupCards();
    }

    private void renderGroupCards() {
        groupsCardsContainer.getChildren().clear();

        if (groups.isEmpty()) {
            Label emptyState = new Label("No groups found.");
            emptyState.getStyleClass().add("groups-empty-state");
            groupsCardsContainer.getChildren().add(emptyState);
            return;
        }

        for (GroupModel group : groups) {
            VBox card = buildGroupCard(group);
            groupsCardsContainer.getChildren().add(card);
        }
    }

    private VBox buildGroupCard(GroupModel group) {
        Label icon = new Label("[]");
        icon.getStyleClass().add("groups-card-icon-text");

        Label name = new Label(group.groupName());
        name.getStyleClass().add("groups-card-title");

        HBox titleRow = new HBox(8.0, icon, name);

        Label description = new Label(group.description());
        description.getStyleClass().add("groups-card-description");
        description.setWrapText(true);

        VBox topContent = new VBox(8.0, titleRow, description);

        Region bodySpacer = new Region();
        VBox.setVgrow(bodySpacer, Priority.ALWAYS);

        Separator divider = new Separator();
        divider.getStyleClass().add("groups-card-divider");

        int expenseCount = expenseCountByGroup.computeIfAbsent(group.groupId(), this::loadExpenseCount);
        Label membersMeta = new Label("Members: " + group.memberCount());
        membersMeta.getStyleClass().add("groups-card-meta");

        Label expensesMeta = new Label("Expenses: " + expenseCount);
        expensesMeta.getStyleClass().add("groups-card-meta-count");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footerRow = new HBox(8.0, membersMeta, footerSpacer, expensesMeta);

        VBox card = new VBox(9.0, topContent, bodySpacer, divider, footerRow);
        card.getStyleClass().add("groups-group-card");
        card.setPrefWidth(388.0);
        card.setPrefHeight(200);
        if (selectedGroup != null && selectedGroup.groupId() == group.groupId()) {
            card.getStyleClass().add("groups-group-card-selected");
        }

        card.setOnMouseClicked(event -> openGroup(group));
        return card;
    }

    private void openGroup(GroupModel group) {
        selectedGroup = group;
        renderGroupCards();
        if (onGroupOpenRequest != null) {
            onGroupOpenRequest.accept(group);
        }
    }

    private int loadExpenseCount(int groupId) {
        ServiceResult<List<com.splitms.models.ExpenseModel>> result = expensesService.listExpensesForGroup(groupId);
        if (!result.success() || result.data() == null) {
            return 0;
        }
        return result.data().size();
    }

    private void selectGroupById(int groupId) {
        GroupModel targetGroup = groups.stream()
                .filter(group -> group.groupId() == groupId)
                .findFirst()
                .orElse(null);
        if (targetGroup != null) {
            openGroup(targetGroup);
        }
    }

    public void setOnGroupOpenRequest(Consumer<GroupModel> onGroupOpenRequest) {
        this.onGroupOpenRequest = onGroupOpenRequest;
    }

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

    private record GroupInput(String name, String description) {
    }
}
