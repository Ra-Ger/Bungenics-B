package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.dto.AdventureEventDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdventureEventMapper {
    public AdventureEvent mapToAdventureEvent(AdventureEventDto dto) {
        AdventureEvent event = new AdventureEvent();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setResult(dto.getResult());
        event.setGoldReward(dto.getGoldReward());
        event.setCarrotReward(dto.getCarrotReward());
        event.setLettuceReward(dto.getLettuceReward());
        event.setSpinachReward(dto.getSpinachReward());
        return event;
    }

    public AdventureEventDto mapToAdventureEventDto(AdventureEvent event) {
        return new AdventureEventDto(
                event.getId(),
                event.getName(),
                event.getResult(),
                event.getGoldReward(),
                event.getCarrotReward(),
                event.getLettuceReward(),
                event.getSpinachReward()
        );
    }

    public List<AdventureEventDto> mapToAdventureEventDtoList(List<AdventureEvent> events) {
        return events.stream()
                .map(this::mapToAdventureEventDto)
                .toList();
    }
}