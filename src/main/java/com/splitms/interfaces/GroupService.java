package com.splitms.interfaces;

import com.splitms.models.GroupModel;
import com.splitms.services.ServiceResult;
import java.util.List;

public interface GroupService {
    ServiceResult<List<GroupModel>> listGroupsForUser(int userId, String searchQuery);

    ServiceResult<GroupModel> createGroup(int userId, String name, String description);

    ServiceResult<GroupModel> updateGroup(int groupId, int ownerUserId, String name, String description);

    ServiceResult<Void> deleteGroup(int groupId, int ownerUserId);
}
