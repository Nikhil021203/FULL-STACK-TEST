package com.example.fullstacktest.item;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data creates the CRUD implementation at runtime. No SQL is needed here.
 */
public interface LabItemRepository extends JpaRepository<LabItem, Long> {
}
