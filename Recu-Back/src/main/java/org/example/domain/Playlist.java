package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="PLAYLISTS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PLAYLIST_ID")
    private Integer playlistId;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;
}
