package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StructureDto {
    private Long id;
    private Long rabbitFarmId;
    private Integer slots;
    private StructureType structureType;
}