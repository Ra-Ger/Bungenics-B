package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.FeedingRecord;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.FeedingDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FeedingMapper {

    public FeedingRecord mapToFeeding(FeedingDto feedingDto) {
        Rabbit rabbit = new Rabbit();
        rabbit.setId(feedingDto.getRabbitId());

        return new FeedingRecord(
                feedingDto.getId(),
                rabbit,
                feedingDto.getFoodType(),
                feedingDto.getAmount(),
                feedingDto.getFeedingTime()
        );
    }

    public FeedingDto mapToFeedingDto(FeedingRecord feedingRecord) {
        return new FeedingDto(
                feedingRecord.getId(),
                feedingRecord.getRabbit() != null ? feedingRecord.getRabbit().getId() : null,
                feedingRecord.getFoodType(),
                feedingRecord.getAmount(),
                feedingRecord.getFeedingTime()
        );
    }

    public List<FeedingDto> mapToFeedingDtoList(List<FeedingRecord> feedings) {
        return feedings.stream().map(this::mapToFeedingDto).toList();
    }
}
