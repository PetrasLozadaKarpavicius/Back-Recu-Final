package org.example.infra;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class LocalEntityManagerProvider {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("database");

    public static EntityManagerFactory get() {
        return emf;
    }
}