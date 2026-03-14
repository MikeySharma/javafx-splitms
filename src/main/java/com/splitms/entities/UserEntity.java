package com.splitms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "user")
    private List<GroupsEntity> groups = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<GroupMemberEntity> groupMemberships = new ArrayList<>();

    protected UserEntity() {
    }

    public UserEntity(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<GroupsEntity> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public void addGroup(GroupsEntity group) {
        if (group == null || groups.contains(group)) {
            return;
        }
        groups.add(group);
        group.setUser(this);
    }

    public void removeGroup(GroupsEntity group) {
        if (group == null || !groups.remove(group)) {
            return;
        }
        group.setUser(null);
    }

    public List<GroupMemberEntity> getGroupMemberships() {
        return Collections.unmodifiableList(groupMemberships);
    }
}
