package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.dto.RoomDto;
import com.kodilla.bungenics.mapper.RoomMapper;
import com.kodilla.bungenics.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomMapper roomMapper;
    private final RoomService roomService;

    @GetMapping
    public List<RoomDto> getRooms() {
        return roomMapper.mapToRoomDtoList(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomMapper.mapToRoomDto(roomService.getRoomById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoomDto> createRoom(@RequestBody RoomDto roomDto) {
        Room room = roomService.createRoom(roomMapper.mapToRoom(roomDto));
        return ResponseEntity.status(201).body(roomMapper.mapToRoomDto(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @RequestBody RoomDto roomDto) {
        Room updated = roomService.updateRoom(id, roomMapper.mapToRoom(roomDto));
        return ResponseEntity.ok(roomMapper.mapToRoomDto(updated));
    }
}