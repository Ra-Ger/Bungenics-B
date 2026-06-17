package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.dto.StructureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StructureMapper {

    private final RoomMapper roomMapper;

    public Structure mapToStructure(StructureDto dto) {
        Structure structure = new Structure();
        structure.setId(dto.getId());
        structure.setSlots(dto.getSlots());
        structure.setStructureType(dto.getStructureType());
        structure.setGridIndex(dto.getGridIndex());

        if (dto.getRabbitFarmId() != null) {
            RabbitFarm farm = new RabbitFarm();
            farm.setId(dto.getRabbitFarmId());
            structure.setRabbitFarm(farm);
        }
        if (dto.getRooms() != null) {
            structure.setRooms(roomMapper.mapToRoomList(dto.getRooms()));
        }
        return structure;
    }

    public StructureDto mapToStructureDto(Structure structure) {
        return new StructureDto(
                structure.getId(),
                structure.getRabbitFarm() != null ? structure.getRabbitFarm().getId() : null,
                structure.getSlots(),
                structure.getStructureType(),
                structure.getGridIndex(),
                structure.getRooms() != null ? roomMapper.mapToRoomDtoList(structure.getRooms()) : new ArrayList<>()
        );
    }

    public List<StructureDto> mapToStructureDtoList(List<Structure> structures) {
        return structures.stream().map(this::mapToStructureDto).toList();
    }
}