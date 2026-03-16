package com.splitms.repositories;

import com.splitms.models.GroupModel;
import java.util.List;
import java.util.Optional;

public interface GroupRepository {
    List<GroupModel> listByOwner(int userId, String searchQuery);

    Optional<GroupModel> findByIdAndOwner(int groupId, int ownerUserId);

    int create(int userId, String groupName, String description, boolean isPersonalDefault);

    boolean update(int groupId, int ownerUserId, String groupName, String description);

    boolean delete(int groupId, int ownerUserId);
}
