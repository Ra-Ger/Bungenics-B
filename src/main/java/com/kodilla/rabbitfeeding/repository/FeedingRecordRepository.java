package com.kodilla.rabbitfeeding.repository;

import com.kodilla.rabbitfeeding.domain.FeedingRecord;
import com.kodilla.rabbitfeeding.domain.Rabbit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord,Long> {
    // findByRabbitId i findByRabbit_Id są poprawne
    /*
    findByRabbitId odwołuje się do pola id w encji Rabbit (czyli private long id w klasie Rabbit oznaczonej @Id).
    Nie ma żadnego związku z tym, jak nazywa się kolumna w tabeli feeding_records (np. rabbit_id),
    ani z tym, jak nazywa się samo pole rabbit w FeedingRecord.
    */
    List<FeedingRecord> findByRabbitId(long rabbitId);
}
