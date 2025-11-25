package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.example.domain.Customer;
import org.example.domain.Invoice;
import org.example.domain.Invoice;


import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;


public class InvoiceRepository {
    private final EntityManager em;
    public InvoiceRepository(EntityManager em) { this.em = em; }

    public Invoice findById(Integer id){
        if (id == null) return null;
        TypedQuery<Invoice> q = em.createQuery("SELECT i FROM Invoice i WHERE i.invoiceId = :id", Invoice.class);
        q.setParameter("id", id);
        try { return q.getSingleResult(); } catch (Exception e) { return null; }
    }

    public Invoice findByCustomerDateTotal(Customer customer, java.util.Date invoiceDate, BigDecimal total) {
        if (customer == null || invoiceDate == null || total == null) return null;
        TypedQuery<Invoice> q = em.createQuery(
                "SELECT i FROM Invoice i WHERE i.customerId = :customer AND i.invoiceDate = :d AND i.total = :tot",
                Invoice.class);
        q.setParameter("customer", customer);
        q.setParameter("d", invoiceDate);
        q.setParameter("tot", total);
        try { return q.getSingleResult(); } catch (Exception e) { return null; }
    }

    public Invoice getOrCreate(Customer customer, Date invoiceDate, BigDecimal total) {
        if (customer == null || invoiceDate == null || total == null) return null;
        Invoice existing = findByCustomerDateTotal(customer, invoiceDate, total);
        if (existing != null) return existing;
        Invoice inv = new Invoice();
        inv.setCustomerId(customer);
        inv.setInvoiceDate(invoiceDate);
        inv.setTotal(total);
        em.persist(inv);
        return inv;
    }
    

}

