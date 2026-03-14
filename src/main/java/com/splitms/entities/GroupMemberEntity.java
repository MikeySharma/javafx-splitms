package com.splitms.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_group_member",
        columnNames = {"group_id", "user_id"}
    )
)
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupsEntity group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected GroupMemberEntity() {
    }

    public GroupMemberEntity(GroupsEntity group, UserEntity user) {
        this.group = group;
        this.user = user;
    }

    @PrePersist
    private void onCreate() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }

    public int getId() {
        return id;
    }

    public GroupsEntity getGroup() {
        return group;
    }

    public UserEntity getUser() {
        return user;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
