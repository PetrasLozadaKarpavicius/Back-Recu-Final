package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.domain.Playlist;
import org.example.domain.PlaylistTrack;
import org.example.domain.Track;

public class PlaylistTrackRepository {

    private final EntityManager em;

    public PlaylistTrackRepository(EntityManager em) {
        this.em = em;
    }

    public boolean exists(Playlist playlist, Track track) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(pt) FROM PlaylistTrack pt WHERE pt.playlist = :playlist AND pt.track = :track",
                Long.class);
        Long count = query.setParameter("playlist", playlist)
                .setParameter("track", track)
                .getSingleResult();
        return count != null && count > 0;
    }

    public PlaylistTrack create(Playlist playlist, Track track) {
        PlaylistTrack pt = new PlaylistTrack();
        pt.setPlaylist(playlist);
        pt.setTrack(track);
        em.persist(pt);
        return pt;
    }
}

