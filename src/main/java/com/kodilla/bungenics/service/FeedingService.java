package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbit.FeedingRecord;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.FeedingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedingService {

    private final FeedingRecordRepository feedingRecordRepository;
    private final RabbitService rabbitService;

    public FeedingRecord saveFeedingRecord(Long rabbitId, FeedingRecord feedingRecord) {
        Rabbit rabbit = rabbitService.getRabbitById(rabbitId);

        FeedingRecord feedingRecordEntity = new FeedingRecord(
                feedingRecord.getId(),
                rabbit,
                feedingRecord.getFoodType(),
                feedingRecord.getAmount(),
                feedingRecord.getFeedingTime()
        );

        // dodawanie nasycenia
        rabbit.setNutritionLevel(Math.min(100f, rabbit.getNutritionLevel() + (feedingRecord.getAmount() * 10)));
        rabbitService.updateRabbit(rabbitId, rabbit);

        return feedingRecordRepository.save(feedingRecordEntity);
    }

    public List<FeedingRecord> getFeedingsForRabbit(Long rabbitId) {
        rabbitService.getRabbitById(rabbitId);
        return feedingRecordRepository.findByRabbitId(rabbitId);
    }

    public FeedingRecord getFeedingRecordById(long feedingRecordId) {
        return feedingRecordRepository.findById(feedingRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Feeding record with id " + feedingRecordId + " not found"));
    }

    public List<FeedingRecord> getAllFeedingRecords() {
        return feedingRecordRepository.findAll();
    }
}