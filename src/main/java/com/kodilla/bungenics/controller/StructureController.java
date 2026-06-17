package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.dto.StructureDto;
import com.kodilla.bungenics.mapper.StructureMapper;
import com.kodilla.bungenics.service.StructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/structures")
public class StructureController {

    private final StructureMapper structureMapper;
    private final StructureService structureService;

    @GetMapping
    public List<StructureDto> getStructures() {
        return structureMapper.mapToStructureDtoList(structureService.getAllStructures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StructureDto> getStructure(@PathVariable Long id) {
        return ResponseEntity.ok(structureMapper.mapToStructureDto(structureService.getStructureById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StructureDto> createStructure(@RequestBody StructureDto structureDto) {
        Structure structure = structureService.createStructure(structureMapper.mapToStructure(structureDto));
        return ResponseEntity.status(201).body(structureMapper.mapToStructureDto(structure));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StructureDto> updateStructure(@PathVariable Long id, @RequestBody StructureDto structureDto) {
        Structure updated = structureService.updateStructure(id, structureMapper.mapToStructure(structureDto));
        return ResponseEntity.ok(structureMapper.mapToStructureDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long id) {
        structureService.deleteStructure(id);
        return ResponseEntity.noContent().build();
    }
}