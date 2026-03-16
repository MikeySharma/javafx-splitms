package com.splitms.controllers;

import com.splitms.models.GroupMemberView;
import com.splitms.models.GroupModel;
import com.splitms.services.ApplicationServices;
import com.splitms.services.GroupMembersService;
import com.splitms.services.GroupsService;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class GroupsContentController {

    private final GroupsService groupsService = ApplicationServices.groupsService();
    private final GroupMembersService membersService = ApplicationServices.groupMembersService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final ObservableList<GroupModel> groups = FXCollections.observableArrayList();
    private final ObservableList<GroupMemberView> members = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;

    @FXML
    private Label groupNameLabel;

    @FXML
    private Label groupDescriptionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField memberEmailField;

    @FXML
    private ListView<GroupModel> groupsListView;

    @FXML
    private ListView<GroupMemberView> membersListView;

    @FXML
    private Button editGroupButton;

    @FXML
    private Button deleteGroupButton;

    @FXML
    private Button addMemberButton;

    @FXML
    private Button removeMemberButton;

    @FXML
    private void initialize() {
        groupsListView.setItems(groups);
        membersListView.setItems(members);

        groupsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GroupModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.groupName() + "  (" + item.memberCount() + " members)");
            }
        });

        membersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GroupMemberView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.name() + " <" + item.email() + ">");
            }
        });

        groupsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            refreshGroupDetails(newValue);
        });

        searchField.textProperty().addListener((obs, oldValue, newValue) -> loadGroups());

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
        groupsListView.getSelectionModel().select(result.data());
    }

    @FXML
    private void onEditGroup() {
        GroupModel selected = groupsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a group first.");
            return;
        }

        Optional<GroupInput> input = showGroupDialog("Edit Group", selected.groupName(), selected.description());
        if (input.isEmpty()) {
            return;
        }

        ServiceResult<GroupModel> result = groupsService.updateGroup(
                selected.groupId(),
                sessionManager.getUserId(),
                input.get().name(),
                input.get().description());

        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        loadGroups();
        groupsListView.getSelectionModel().select(result.data());
    }

    @FXML
    private void onDeleteGroup() {
        GroupModel selected = groupsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a group first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Group");
        confirm.setHeaderText("Delete " + selected.groupName() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> confirmResult = confirm.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        ServiceResult<Void> result = groupsService.deleteGroup(selected.groupId(), sessionManager.getUserId());
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        loadGroups();
    }

    @FXML
    private void onAddMember() {
        GroupModel selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Select a group first.");
            return;
        }

        ServiceResult<Void> result = membersService.addMemberByEmail(selectedGroup.groupId(), memberEmailField.getText());
        if (!result.success()) {
            showError(result.message());
            return;
        }

        memberEmailField.clear();
        showSuccess(result.message());
        loadMembers(selectedGroup.groupId());
        loadGroups();
    }

    @FXML
    private void onRemoveMember() {
        GroupModel selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
        GroupMemberView selectedMember = membersListView.getSelectionModel().getSelectedItem();

        if (selectedGroup == null || selectedMember == null) {
            showError("Select both a group and a member.");
            return;
        }

        ServiceResult<Void> result = membersService.removeMember(selectedGroup.groupId(), selectedMember.userId());
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess(result.message());
        loadMembers(selectedGroup.groupId());
        loadGroups();
    }

    private void loadGroups() {
        ServiceResult<List<GroupModel>> result = groupsService.listGroupsForUser(
                sessionManager.getUserId(),
                searchField.getText());

        groups.setAll(result.data() == null ? List.of() : result.data());
        if (!groups.isEmpty()) {
            groupsListView.getSelectionModel().selectFirst();
        } else {
            refreshGroupDetails(null);
        }
    }

    private void refreshGroupDetails(GroupModel group) {
        boolean hasSelection = group != null;
        editGroupButton.setDisable(!hasSelection);
        deleteGroupButton.setDisable(!hasSelection);
        addMemberButton.setDisable(!hasSelection);
        removeMemberButton.setDisable(!hasSelection);

        if (!hasSelection) {
            groupNameLabel.setText("No group selected");
            groupDescriptionLabel.setText("Choose a group from the left list.");
            members.clear();
            return;
        }

        groupNameLabel.setText(group.groupName());
        groupDescriptionLabel.setText(group.description());
        loadMembers(group.groupId());
    }

    private void loadMembers(int groupId) {
        ServiceResult<List<GroupMemberView>> result = membersService.listMembers(groupId);
        members.setAll(result.data() == null ? List.of() : result.data());
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
