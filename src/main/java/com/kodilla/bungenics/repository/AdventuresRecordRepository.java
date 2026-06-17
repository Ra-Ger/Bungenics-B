package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventuresRecordRepository extends JpaRepository<AdventuresRecord, Long> {
}