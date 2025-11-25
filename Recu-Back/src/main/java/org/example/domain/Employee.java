package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "EMPLOYEES")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {

    @Id
    @SequenceGenerator(name = "employee_seq", sequenceName = "SEQ_EMPLOYEE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @Column(name = "EMPLOYEE_ID")
    private Integer id_employee;

    @Column(name = "LAST_NAME", length = 20, nullable = false)
    private String lastName;

    @Column(name = "FIRST_NAME", length = 20, nullable = false)
    private String firstName;

    @Column(name = "TITLE", length = 30)
    private String title;

    // Empleado al que reporta (puede ser null)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REPORTS_TO") // <-- nombre real de la FK
    private Employee reportsTo;  // Autorreferencia

    @Temporal(TemporalType.DATE)
    @Column(name = "BIRTH_DATE")
    private Date birthDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "HIRE_DATE")
    private Date hireDate;

    @Column(name = "ADDRESS", length = 70)
    private String address;

    @Column(name = "CITY", length = 40)
    private String city;

    @Column(name = "STATE", length = 40)
    private String state;

    @Column(name = "COUNTRY", length = 40)
    private String country;

    @Column(name = "POSTAL_CODE", length = 10)
    private String postalCode;

    @Column(name = "PHONE", length = 24)
    private String phone;

    @Column(name = "FAX", length = 24)
    private String fax;

    @Column(name = "EMAIL", length = 60)
    private String email;

    public Integer getId() {
        return this.id_employee;
    }
}
