package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="ARTISTS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Artist {
    @Id
    @SequenceGenerator(name = "artist_seq", sequenceName = "SEQ_ARTIST_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq")
    @Column(name="ARTIST_ID")
    private Integer artistid;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;

}
