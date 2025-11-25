package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Playlist;

public class PlaylistRepository {

    private final EntityManager em;

    public PlaylistRepository(EntityManager em) {
        this.em = em;
    }

    public Playlist findByName(String name) {
        if (name == null) {
            return null;
        }
        TypedQuery<Playlist> query = em.createQuery(
                "SELECT p FROM Playlist p WHERE UPPER(p.name) = :name", Playlist.class);
        return query.setParameter("name", name.trim().toUpperCase()).getResultStream().findFirst().orElse(null);
    }

    public Playlist getOrCreate(String name) {
        if (name == null) {
            return null;
        }
        Playlist existing = findByName(name);
        if (existing != null) {
            return existing;
        }
        Playlist playlist = new Playlist();
        playlist.setName(name.trim());
        em.persist(playlist);
        return playlist;
    }
}

