package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.dto.AdventureDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdventureMapper {

    public Adventure mapToAdventure(AdventureDto dto) {
        Adventure adventure = new Adventure();
        adventure.setName(dto.getName());
        adventure.setPlayerId(dto.getPlayerId());
        adventure.setRabbitId(dto.getRabbitId());
        adventure.setType(dto.getType());
        adventure.setEndTime(dto.getEndTime());
        adventure.setStatus(dto.getStatus());
        adventure.setAdventureEvents(dto.getAdventureEvents());

        return adventure;
    }

    public AdventureDto mapToAdventureDto(Adventure adventure) {
        return new AdventureDto(
                adventure.getId(),
                adventure.getName(),
                adventure.getPlayerId(),
                adventure.getRabbitId(),
                adventure.getType(),
                adventure.getEndTime(),
                adventure.getStatus(),
                adventure.getAdventureEvents()
        );
    }

    public List<AdventureDto> mapToAdventureDtoList(List<Adventure> adventures) {
        return adventures.stream().map(this::mapToAdventureDto).toList();
    }
}