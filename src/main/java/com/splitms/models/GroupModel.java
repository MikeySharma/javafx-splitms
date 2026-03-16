package com.splitms.models;

import com.splitms.interfaces.Groups;

public record GroupModel(
        int groupId,
        int ownerUserId,
        String groupName,
        String description,
        boolean personalDefault,
        int memberCount) implements Groups {

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isPersonalDefault() {
        return personalDefault;
    }
}
