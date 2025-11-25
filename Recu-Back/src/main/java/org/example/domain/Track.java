package org.example.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="TRACKS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Track {
    @Id
    @SequenceGenerator(name = "track_seq", sequenceName = "SEQ_TRACK_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq")
    @Column(name="TRACK_ID")
    private Integer trackId;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name="ALBUM_ID", nullable = false)
    private Album album;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="MEDIA_TYPE_ID", nullable = false)
    private MediaType mediaType;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name="GENRE_ID", nullable = true)
    private Genre genre;

    @Column(name="COMPOSER", length = 220)
    private String composer;
    @Column(name="NAME", length = 200, nullable = false)
    private String name;

    @Column(name="MILLISECONDS", nullable = false)
    private Integer milliseconds;
    @Column(name="BYTES", nullable = false)
    private Integer bytes;
    @Column(name="UNIT_PRICE",precision = 10,scale = 2, nullable = false)
    private BigDecimal unitPrice;

    //metodos
    // @Transient de JPA le indica al proveedor de persistencia que NO debe mapear ese campo o metodo a una columna de la base de datos. Se usa para propiedades calculadas o auxiliares que no forman
    // parte del modelo persistente
    @Transient  // no se persiste en la BD
    public double getDurationInMinutes() {
        return milliseconds != null ? milliseconds / 60000.0 : 0.0;
    }
    @Transient
    public boolean hasValidPrice() {
        return unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
    }
    // getters/setters, constructores, mapeos JPA según corresponda

}
