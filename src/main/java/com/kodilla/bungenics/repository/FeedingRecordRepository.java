package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbit.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord,Long> {
    List<FeedingRecord> findByRabbitId(long rabbitId);
}
