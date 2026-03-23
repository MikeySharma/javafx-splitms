package com.splitms.controllers;

import com.splitms.models.GroupModel;
import com.splitms.services.ApplicationServices;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class GroupDetailsContentController {

    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private GroupModel group;
    private Runnable onBackRequest;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label statusLabel;

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
