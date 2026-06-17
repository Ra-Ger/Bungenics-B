package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.dto.RoomDto;
import com.kodilla.bungenics.dto.StructureDto;
import com.kodilla.bungenics.mapper.RoomMapper;
import com.kodilla.bungenics.mapper.StructureMapper;
import com.kodilla.bungenics.service.StructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/structures")
public class StructureController {

    private final StructureMapper structureMapper;
    private final RoomMapper roomMapper;
    private final StructureService structureService;

    @GetMapping
    public List<StructureDto> getStructures() {
        return structureMapper.mapToStructureDtoList(structureService.getAllStructures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StructureDto> getStructure(@PathVariable Long id) {
        return ResponseEntity.ok(structureMapper.mapToStructureDto(structureService.getStructureById(id)));
    }

    @PostMapping("/build")
    public ResponseEntity<StructureDto> buildStructure(
            @RequestParam Long farmId,
            @RequestParam StructureType type,
            @RequestParam Integer gridIndex) {
        Structure structure = structureService.buildStructure(farmId, type, gridIndex);
        return ResponseEntity.status(201).body(structureMapper.mapToStructureDto(structure));
    }

    @PostMapping("/{id}/add-room")
    public ResponseEntity<StructureDto> addRoom(@PathVariable Long id) {
        Structure updated = structureService.addRoomToStructure(id);
        return ResponseEntity.ok(structureMapper.mapToStructureDto(updated));
    }

    @PostMapping("/rooms/{roomId}/expand")
    public ResponseEntity<RoomDto> expandRoom(@PathVariable Long roomId) {
        Room expanded = structureService.expandRoomSlots(roomId);
        return ResponseEntity.ok(roomMapper.mapToRoomDto(expanded));
    }

    @PostMapping("/rooms/{roomId}/assign")
    public ResponseEntity<RoomDto> assignRabbit(@PathVariable Long roomId, @RequestParam Long rabbitId) {
        Room updated = structureService.assignRabbitToRoom(roomId, rabbitId);
        return ResponseEntity.ok(roomMapper.mapToRoomDto(updated));
    }

    @PostMapping("/rooms/{roomId}/remove")
    public ResponseEntity<RoomDto> removeRabbit(@PathVariable Long roomId, @RequestParam Long rabbitId) {
        Room updated = structureService.removeRabbitFromRoom(roomId, rabbitId);
        return ResponseEntity.ok(roomMapper.mapToRoomDto(updated));
    }

    @PostMapping("/rooms/{roomId}/breed")
    public ResponseEntity<RoomDto> startBreeding(@PathVariable Long roomId) {
        Room updated = structureService.startBreedingInRoom(roomId);
        return ResponseEntity.ok(roomMapper.mapToRoomDto(updated));
    }

    @PostMapping("/rooms/{roomId}/train")
    public ResponseEntity<RoomDto> startTraining(
            @PathVariable Long roomId,
            @RequestParam Long rabbitId,
            @RequestParam(required = false) String foodType) {
        Room updated = structureService.startTrainingInRoom(roomId, rabbitId, foodType);
        return ResponseEntity.ok(roomMapper.mapToRoomDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long id) {
        structureService.deleteStructure(id);
        return ResponseEntity.noContent().build();
    }
}