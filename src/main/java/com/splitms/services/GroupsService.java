package com.splitms.services;

import java.sql.SQLException;

import com.splitms.lib.Database;
import com.splitms.utils.Validation;

public class GroupsService {
    private Integer groupId;
    private String groupName;
    private String description;
    private boolean isPersonalDefault;

    public GroupsService() {
        this.groupId = null;
        this.groupName = "";
        this.description = "";
        this.isPersonalDefault = false;
    }

    public GroupsService(String groupName, String description, boolean isPersonalDefault) {
        this.groupId = null;
        this.groupName = groupName;
        this.description = description;
        this.isPersonalDefault = isPersonalDefault;
    }

    public static boolean createDefaultPersonalGroupForUser(int userId) {
        if (userId <= 0) {
            return false;
        }

        String defaultGroupName = "Personal Group";
        String defaultDescription = "This is your personal default group.";
        String sql = "INSERT INTO `group` (user_id, group_name, description, is_personal_default) VALUES (" +
            userId + ", '" +
                Validation.escapeSql(defaultGroupName) + "', '" +
                Validation.escapeSql(defaultDescription) + "', " +
                true + ")";

        try {
            int affectedRows = Database.executeUpdate(sql);
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

}
