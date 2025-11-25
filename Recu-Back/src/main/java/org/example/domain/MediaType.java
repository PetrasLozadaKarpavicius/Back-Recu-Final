package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="MEDIA_TYPES")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaType {
    @Id
    @SequenceGenerator(name = "media_type_seq", sequenceName = "SEQ_MEDIA_TYPE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_type_seq")
    @Column(name="MEDIA_TYPE_ID")
    private Integer mediaTypeId;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;
}
