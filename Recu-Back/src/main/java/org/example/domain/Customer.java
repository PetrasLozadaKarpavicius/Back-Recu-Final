package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="CUSTOMERS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CUSTOMER_ID")
    private Integer customerId;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "SUPPORT_REP_ID", nullable = false)
    private Employee supportRepId;
    @Column(name="FIRST_NAME", length = 40, nullable = false)
    private String firstName;
    @Column(name="LAST_NAME", length = 20, nullable = false)
    private String lastName;
    @Column(name="COMPANY", length = 80)
    private String company;
    @Column(name="ADDRESS", length = 70)
    private String address;
    @Column(name="CITY", length = 40)
    private String city;
    @Column(name="STATE", length = 40)
    private String state;
    @Column(name="COUNTRY", length = 40)
    private String country;
    @Column(name="POSTAL_CODE", length = 10)
    private String postalCode;
    @Column(name="PHONE", length = 24)
    private String phone;
    @Column(name="FAX", length = 24)
    private String fax;
    @Column(name="EMAIL", length = 60)
    private String email;



}
