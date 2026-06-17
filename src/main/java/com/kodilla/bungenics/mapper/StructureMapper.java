package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.dto.StructureDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StructureMapper {

    public Structure mapToStructure(StructureDto dto) {
        Structure structure = new Structure();
        structure.setId(dto.getId());
        structure.setSlots(dto.getSlots());
        structure.setStructureType(dto.getStructureType());

        if (dto.getRabbitFarmId() != null) {
            RabbitFarm farm = new RabbitFarm();
            farm.setId(dto.getRabbitFarmId());
            structure.setRabbitFarm(farm);
        }
        return structure;
    }

    public StructureDto mapToStructureDto(Structure structure) {
        return new StructureDto(
                structure.getId(),
                structure.getRabbitFarm() != null ? structure.getRabbitFarm().getId() : null,
                structure.getSlots(),
                structure.getStructureType()
        );
    }

    public List<StructureDto> mapToStructureDtoList(List<Structure> structures) {
        return structures.stream().map(this::mapToStructureDto).toList();
    }
}