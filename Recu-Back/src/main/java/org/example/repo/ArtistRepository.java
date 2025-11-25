package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Artist;

public class ArtistRepository {
    private final EntityManager em;

    public ArtistRepository(EntityManager em) { this.em = em; }

    public Artist findbyName(String name) {
        if (name == null) return null;
        TypedQuery<Artist> q= em.createQuery(
                "SELECT a FROM Artist a WHERE UPPER(a.name) = :name", Artist.class)
                .setParameter("name", name.trim().toUpperCase());
        return q.getResultStream().findFirst().orElse(null);
    }

    public Artist getOrCreate(String name) {
        if (name == null) return null;
        Artist existente = findbyName(name);
        if (existente != null) return existente;
        Artist a = new Artist();
        a.setName(name);
        em.persist(a);
        return a;
    }
}