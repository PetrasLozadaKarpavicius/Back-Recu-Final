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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MEDIA_TYPE_ID")
    private Integer mediaTypeId;

    @Column(name="NAME", length = 120, nullable = false)
    private String name;
}
