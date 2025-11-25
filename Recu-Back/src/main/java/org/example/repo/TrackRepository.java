package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Album;
import org.example.domain.Genre;
import org.example.domain.MediaType;
import org.example.domain.Track;

import java.math.BigDecimal;

public class TrackRepository {
    private final EntityManager em;

    public TrackRepository(EntityManager em) { this.em = em; }

    public Track findByNameAndAlbum(String name, Album album) {
        if (name == null) return null;
        if (album != null) {
            TypedQuery<Track> q = em.createQuery(
                    "SELECT t FROM Track t WHERE UPPER(t.name) = :name AND t.album = :album", Track.class);
            q.setParameter("name", name.trim().toUpperCase());
            q.setParameter("album", album);
            return q.getResultStream().findFirst().orElse(null);
        }
        return findByName(name);
    }

    public Track findByName(String name) {
        if (name == null) return null;
        TypedQuery<Track> q = em.createQuery(
                "SELECT t FROM Track t WHERE UPPER(t.name) = :name", Track.class);
        return q.setParameter("name", name.trim().toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Track getOrCreate(String name, Album album, MediaType mediaType, Genre genre,
                             String composer, Integer milliseconds, Integer bytes, BigDecimal unitPrice) {
        if (name == null) return null;
        Track existing = album != null ? findByNameAndAlbum(name, album) : findByName(name);
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