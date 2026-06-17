package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.adventure.Adventure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureRepository extends JpaRepository<Adventure, Long> {
}