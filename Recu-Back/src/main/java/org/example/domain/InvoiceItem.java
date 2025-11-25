package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="INVOICE_ITEMS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="INVOICE_LINE_ID")
    private Integer invoiceLineId;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="INVOICE_ID", nullable = false)
    private Invoice invoiceId;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="TRACK_ID", nullable = false)
    private Track trackId;
    @Column(name="UNIT_PRICE",precision = 10,scale = 2, nullable = false)
    private BigDecimal unitPrice;
    @Column(name="QUANTITY", nullable = false)
    private Integer quantity;

}
