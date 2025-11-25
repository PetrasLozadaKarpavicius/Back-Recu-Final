package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="ALBUMS")
@Builder

public class Album {
    @Id
    @SequenceGenerator(name = "album_seq", sequenceName = "SEQ_ALBUM_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq")
    @Column(name="ALBUM_ID")
    private Integer albumId;
    @Column(name="TITLE", length = 160, nullable = false)
    private String title;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="ARTIST_ID", nullable = false)
    private Artist artistId;



    //metodos



}
