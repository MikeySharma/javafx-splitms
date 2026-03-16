package com.splitms.interfaces;

import com.splitms.models.GroupMemberView;
import com.splitms.services.ServiceResult;
import java.util.List;

public interface GroupMembershipService {
    ServiceResult<List<GroupMemberView>> listMembers(int groupId);

    ServiceResult<Void> addMemberByEmail(int groupId, String email);

    ServiceResult<Void> removeMember(int groupId, int userId);
}
