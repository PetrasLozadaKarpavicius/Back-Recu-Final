package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Album;
import org.example.domain.Artist;


import java.sql.*;
import java.util.List;

public class AlbumRepository {
    private final EntityManager em;

    public AlbumRepository(EntityManager em) { this.em = em; }

    public Album findById(Integer id){ //busca por PK
        if (id == null) return null;
        TypedQuery<Album> q = em.createQuery(
                "SELECT a FROM Album a WHERE a.albumId = :id", Album.class);
        q.setParameter("id", id);
        try { return q.getSingleResult();
        } catch (Exception e) { return null; }
    }

    public Album findByTitle(String title){ //busca por titulo
        TypedQuery<Album> q = em.createQuery(
                "SELECT a FROM Album a WHERE a.title = :title", Album.class);
        q.setParameter("title", title);
        try { return q.getSingleResult();
        } catch (Exception e) { return null; }
    }

    public List<Album> findAll(){
        TypedQuery<Album> q = em.createQuery(
                "SELECT a FROM Album a ORDER BY a.title", Album.class);
        return q.getResultList();
    }

    public Album getOrCreate(String title, Artist artist) {
        if (title == null) return null;
        // Buscar por título (y opcionalmente por artista si existe)
        if (artist != null) {
            TypedQuery<Album> q = em.createQuery(
                    "SELECT a FROM Album a WHERE a.title = :title AND a.artistId = :artist", Album.class);
            q.setParameter("title", title);
            q.setParameter("artist", artist);
            try { return q.getSingleResult(); } catch (Exception e) { /* not found */ }
        } else {
            Album existing = findByTitle(title);
            if (existing != null) return existing;
        }
        Album a = new Album();
        a.setTitle(title);
        a.setArtistId(artist);
        em.persist(a);
        return a;
    }

}