package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.*;
import org.example.infra.DataSourceProvider;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackRepository {
    private final EntityManager em;

    public TrackRepository(EntityManager em) { this.em = em; }

    public Track findByNameAndAlbum(String name, Album album) {
        if (name == null) return null;
        if (album != null) {
            TypedQuery<Track> q = em.createQuery(
                    "SELECT t FROM Track t WHERE t.name = :name AND t.album = :album", Track.class);
            q.setParameter("name", name);
            q.setParameter("album", album);
            try { return q.getSingleResult(); } catch (Exception e) { return null; }
        } else {
            TypedQuery<Track> q = em.createQuery(
                    "SELECT t FROM Track t WHERE t.name = :name", Track.class);
            q.setParameter("name", name);
            try { return q.getSingleResult(); } catch (Exception e) { return null; }
        }
    }

    public Track getOrCreate(String name, Album album, MediaType mediaType, Genre genre,
                             String composer, Integer milliseconds, Integer bytes, BigDecimal unitPrice) {
        if (name == null) return null;
        Track existing = findByNameAndAlbum(name, album);
        if (existing != null) return existing;
        Track t = new Track();
        t.setName(name);
        t.setAlbum(album);
        t.setMediaType(mediaType);
        t.setGenre(genre);
        t.setComposer(composer);
        t.setMilliseconds(milliseconds);
        t.setBytes(bytes);
        t.setUnitPrice(unitPrice);
        em.persist(t);
        return t;
    }

}