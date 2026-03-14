package com.splitms.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "`group`")
public class GroupsEntity {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private int groupId;

    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "is_personal_default", nullable = false)
    private boolean isPersonalDefault;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "group")
    private List<GroupMemberEntity> members = new ArrayList<>();

    protected GroupsEntity() {
    }

    public GroupsEntity(String groupName, String description, boolean isPersonalDefault) {
        this.groupName = groupName;
        this.description = description;
        this.isPersonalDefault = isPersonalDefault;
    }

    public GroupsEntity(String groupName, String description, boolean isPersonalDefault, UserEntity user) {
        this.groupName = groupName;
        this.description = description;
        this.isPersonalDefault = isPersonalDefault;
        this.user = user;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPersonalDefault() {
        return isPersonalDefault;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<GroupMemberEntity> getMembers() {
        return Collections.unmodifiableList(members);
    }

}