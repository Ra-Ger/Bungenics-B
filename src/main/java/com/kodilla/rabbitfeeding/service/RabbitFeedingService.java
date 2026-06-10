package com.kodilla.rabbitfeeding.service;

import com.kodilla.rabbitfeeding.domain.FeedingRecord;
import com.kodilla.rabbitfeeding.domain.Rabbit;
import com.kodilla.rabbitfeeding.exception.ResourceNotFoundException;
import com.kodilla.rabbitfeeding.repository.FeedingRecordRepository;
import com.kodilla.rabbitfeeding.repository.RabbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitFeedingService {
    private final RabbitRepository rabbitRepository;
    private final FeedingRecordRepository feedingRecordRepository;

    public Rabbit createRabbit(Rabbit rabbit)
    {
        rabbitRepository.save(rabbit);
        return rabbit;
    }

    public Rabbit getRabbitById(long rabbitId)
    {
        return rabbitRepository.findById(rabbitId).orElseThrow(() -> new ResourceNotFoundException("Rabbit with id " + rabbitId + " not found"));
    }

    public List<Rabbit> getAllRabbits()
    {
        return rabbitRepository.findAll();
    }

    public FeedingRecord addFeedingRecord(Long rabbitId, FeedingRecord feedingRecord)
    {
        // tez zwroci blond jezeli brak krolika bo ma findRabbitById
        FeedingRecord feedingRecordEntity = new FeedingRecord(null, getRabbitById(rabbitId),
                feedingRecord.getFoodType(),feedingRecord.getAmount(),feedingRecord.getFeedingTime());
        feedingRecordRepository.save(feedingRecordEntity);
        return feedingRecordEntity;
    }

    public List<FeedingRecord> getFeedingsForRabbit(Long rabbitId)
    {
        getRabbitById(rabbitId); // zwróci błąd jeżeli podasz złe id
        return feedingRecordRepository.findByRabbitId(rabbitId);
    }

    public FeedingRecord getFeedingRecordById(long feedingRecordId)
    {
        return feedingRecordRepository.findById(feedingRecordId).orElseThrow(() -> new ResourceNotFoundException("Feeding recordId with id " + feedingRecordId + " not found"));
    }

    public List<FeedingRecord> getAllFeedingRecords()
    {
        return feedingRecordRepository.findAll();
    }
}
