package com.splitms.services;

import com.splitms.entities.UserEntity;
import com.splitms.lib.Jpa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;

public class UserService {

    // variable declarations
    private Long userId;
    private String name;
    private String email;

    // default constructor
    public UserService() {
        this.userId = null;
        this.name = "";
        this.email = "";
    }

    // parameterized constructor
    public UserService(String name, String email, String password) {
        this.userId = null;
        this.name = name;
        this.email = email;
    }

    // helper method to hash passwords (for demonstration purposes only)
    private static String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }

    // login method
    public long login(String email, String password) {
        String hashed = hashPassword(password);

        try (EntityManager entityManager = Jpa.openEntityManager()) {
            UserEntity found = entityManager
                    .createQuery("select u from UserEntity u where u.email = :email", UserEntity.class)
                    .setParameter("email", email)
                    .getSingleResult();

            if (hashed.equals(found.getPasswordHash())) {
                this.userId = found.getId();
                this.name = found.getName();
                this.email = found.getEmail();
                return this.userId;
            }
        } catch (NoResultException ignored) {
            return -1;
        } catch (RuntimeException e) {
            throw new RuntimeException("Database operation failed: " + e.getMessage(), e);
        }

        return -1;
    }

    // register method
    public boolean register(String name, String email, String password) {
        String hashed = hashPassword(password);

        try (EntityManager entityManager = Jpa.openEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            UserEntity entity = new UserEntity(name, email, hashed);
            entityManager.persist(entity);
            transaction.commit();

            this.userId = entity.getId();
            this.name = name;
            this.email = email;
            return true;
        } catch (PersistenceException e) {
            this.userId = null;
            this.name = "";
            this.email = "";
            return false;
        }
    }

    /**
     * Delete a user by email (for testing purposes)
     */
    public static void deleteByEmail(String email) {
        try (EntityManager entityManager = Jpa.openEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createQuery("delete from UserEntity u where u.email = :email")
                    .setParameter("email", email)
                    .executeUpdate();
            transaction.commit();
        }
    }

    // getUserName method
    public String getUserName() {
        return this.name;
    }

    // getUserEmail method
    public String getUserEmail() {
        return this.email;
    }

}
