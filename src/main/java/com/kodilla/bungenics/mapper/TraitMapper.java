package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.Trait;
import com.kodilla.bungenics.dto.TraitDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TraitMapper {

    public TraitDto mapToTraitDto(Trait trait) {
        if (trait == null) return null;
        Long rabbitId = (trait.getRabbit() != null) ? trait.getRabbit().getId() : null;

        return new TraitDto(
                trait.getId(),
                rabbitId,
                trait.getRabbitTrait()
        );
    }

    public Trait mapToTrait(TraitDto traitDto) {
        if (traitDto == null) return null;

        Trait trait = new Trait();
        trait.setId(traitDto.getId());
        trait.setRabbitTrait(traitDto.getRabbitTrait());
        return trait;
    }

    public List<Trait> mapToTraitList(List<TraitDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::mapToTrait).toList();
    }

    public List<TraitDto> mapToTraitDtoList(List<Trait> traits) {
        if (traits == null) return null;
        return traits.stream().map(this::mapToTraitDto).toList();
    }
}