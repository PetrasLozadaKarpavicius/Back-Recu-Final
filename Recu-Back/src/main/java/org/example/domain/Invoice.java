package org.example.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="INVOICES")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="INVOICE_ID")
    private Integer invoiceId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="CUSTOMER_ID", nullable = false)
    private Customer customerId;

    @Column(name="INVOICE_DATE", nullable = false)
    private Date invoiceDate;

    @Column(name="BILLING_ADDRESS", length = 70)
    private String billingAddress;

    @Column(name="BILLING_CITY", length = 40)
    private String billingCity;

    @Column(name="BILLING_STATE", length = 40)
    private String billingState;

    @Column(name="BILLING_COUNTRY", length = 40)
    private String billingCountry;

    @Column(name="BILLING_POSTAL_CODE", length = 10)
    private String billingPostalCode;
    @Column(name="TOTAL",precision = 10,scale = 2, nullable = false)
    private BigDecimal total;

    @Transient
    public boolean isTotalConsistent() {
        return total != null && total.compareTo(BigDecimal.ZERO) > 0;
    }
}
