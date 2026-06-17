package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.dto.AdventureDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdventureMapper {

    public Adventure mapToAdventure(AdventureDto dto) {
        Adventure adventure = new Adventure();

        return adventure;
    }

    public AdventureDto mapToAdventureDto(Adventure adventure) {
        return new AdventureDto(
                adventure.getId(),
                adventure.getName()
        );
    }

    public List<AdventureDto> mapToAdventureDtoList(List<Adventure> adventures) {
        return adventures.stream().map(this::mapToAdventureDto).toList();
    }
}