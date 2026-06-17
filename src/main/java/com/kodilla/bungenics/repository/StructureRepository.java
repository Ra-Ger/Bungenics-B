package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {
}