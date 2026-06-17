package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureEventRepository extends JpaRepository<AdventureEvent, Long> {
}