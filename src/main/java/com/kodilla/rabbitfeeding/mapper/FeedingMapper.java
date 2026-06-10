package com.kodilla.rabbitfeeding.mapper;

import com.kodilla.rabbitfeeding.domain.FeedingRecord;
import com.kodilla.rabbitfeeding.domain.Rabbit;
import com.kodilla.rabbitfeeding.dto.FeedingDto;
import com.kodilla.rabbitfeeding.dto.RabbitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedingMapper {

    public FeedingRecord mapToFeeding(FeedingDto feedingDto) {
        return new FeedingRecord(feedingDto.getId(),
                feedingDto.getRabbit(),
                feedingDto.getFoodType(),
                feedingDto.getAmount(),
                feedingDto.getFeedingTime());
    }

    public FeedingDto mapToFeedingDto(FeedingRecord feedingRecord) {
        return new FeedingDto(feedingRecord.getId(),
                feedingRecord.getRabbit(),
                feedingRecord.getFoodType(),
                feedingRecord.getAmount(),
                feedingRecord.getFeedingTime());
    }

    public List<FeedingDto> mapToFeedingDtoList(List<FeedingRecord> reedings) {
        return reedings.stream().map(this::mapToFeedingDto).toList();
    }
    
}
