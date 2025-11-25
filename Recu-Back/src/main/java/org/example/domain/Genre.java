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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="GENRE_ID")
    private Integer genreId;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;
}
