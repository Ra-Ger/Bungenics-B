package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.AdventuresRecordDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdventuresRecordMapper {

    public AdventuresRecord mapToAdventuresRecord(AdventuresRecordDto dto) {
        AdventuresRecord record = new AdventuresRecord();
        record.setId(dto.getId());

        if (dto.getRabbitId() != null) {
            Rabbit rabbit = new Rabbit();
            rabbit.setId(dto.getRabbitId());
            record.setRabbit(rabbit);
        }
        return record;
    }

    public AdventuresRecordDto mapToAdventuresRecordDto(AdventuresRecord record) {
        return new AdventuresRecordDto(
                record.getId(),
                record.getRabbit() != null ? record.getRabbit().getId() : null
        );
    }

    public List<AdventuresRecordDto> mapToAdventuresRecordDtoList(List<AdventuresRecord> records) {
        return records.stream().map(this::mapToAdventuresRecordDto).toList();
    }
}