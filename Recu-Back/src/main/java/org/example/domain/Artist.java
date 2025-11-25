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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ARTIST_ID")
    private Integer artistid;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;

}
