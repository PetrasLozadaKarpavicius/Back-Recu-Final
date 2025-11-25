package org.example.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="GENRES")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Genre {
    @Id
    @SequenceGenerator(name = "genre_seq", sequenceName = "SEQ_GENRE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genre_seq")
    @Column(name="GENRE_ID")
    private Integer genreId;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;
}
