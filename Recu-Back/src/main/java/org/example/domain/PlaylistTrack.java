package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="PLAYLIST_TRACK")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistTrack {
    @Id
    @SequenceGenerator(name = "playlist_track_seq", sequenceName = "SEQ_PLAYLIST_TRACK_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "playlist_track_seq")
    @Column(name="PLAYLIST_TRACK_ID")
    private Integer playlistTrackId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="PLAYLIST_ID", nullable = false)
    private Playlist playlistId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="TRACK_ID", nullable = false)
    private Track trackId;
}
