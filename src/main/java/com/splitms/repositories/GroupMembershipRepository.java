package com.splitms.repositories;

import com.splitms.models.GroupMemberView;
import java.util.List;

public interface GroupMembershipRepository {
    List<GroupMemberView> listMembers(int groupId);

    boolean addMember(int groupId, int userId);

    boolean removeMember(int groupId, int userId);

    boolean isMember(int groupId, int userId);
}
