package com.splitms.lib;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class Jpa {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            Persistence.createEntityManagerFactory("splitms");

    private Jpa() {
    }

    public static EntityManager openEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }
}
