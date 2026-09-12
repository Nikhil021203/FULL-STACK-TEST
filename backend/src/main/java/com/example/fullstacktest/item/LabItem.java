package com.example.fullstacktest.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Hibernate maps this Java class to the PostgreSQL table named lab_items.
 */
@Entity
@Table(name = "lab_items")
public class LabItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LabItem() {
        // Required by JPA/Hibernate.
    }

    public LabItem(String title, String note) {
        this.title = title;
        this.note = note;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }

    public void update(String title, String note) {
        this.title = title;
        this.note = note;
    }
}
